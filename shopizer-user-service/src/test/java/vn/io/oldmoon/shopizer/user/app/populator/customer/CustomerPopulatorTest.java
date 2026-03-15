package vn.io.oldmoon.shopizer.user.app.populator.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.AvatarResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.profile.UpdateCustomerProfileRequest;
import vn.io.oldmoon.shopizer.user.app.populator.converter.UrlConverter;
import vn.io.oldmoon.shopizer.user.infra.data.constant.KeycloakRequiredAction;
import vn.io.oldmoon.shopizer.user.infra.model.AvatarMeta;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

@TestPropertySource(
    properties = {
      "minio.endpoint=http://localhost:9000",
      "minio.accessKey=testuser",
      "minio.secretKey=testpassword"
    })
@ExtendWith(MockitoExtension.class) // <--- THIS IS THE MISSING PIECE
class CustomerPopulatorTest {
  @Mock private CustomerMapper customerMapper;

  @Mock private UrlConverter urlConverter;

  @InjectMocks private CustomerPopulator populator;

  private CustomerProfile createTestProfile() {
    CustomerProfile profile = new CustomerProfile();
    profile.setUserId(UUID.randomUUID());
    profile.setUsername("testuser");

    AvatarMeta meta = new AvatarMeta("my-bucket", "orig.jpg", "med.jpg", "thumb.jpg");
    profile.setAvatarMeta(meta);
    return profile;
  }

  @Test
  void toUserRep_ShouldMapToUserRepresentationCorrectly() {
    // GIVEN
    String username = "testuser";
    PersistableCustomer dto = new PersistableCustomer();
    dto.setUsername(username);

    UserRepresentation mockRep = new UserRepresentation();
    mockRep.setUsername(username);

    when(customerMapper.toUserRep(dto)).thenReturn(mockRep);

    // WHEN
    UserRepresentation result = populator.toUserRep(dto);

    // THEN
    assertThat(result.isEnabled()).isTrue();
    assertThat(result.isEmailVerified()).isFalse();
    assertThat(result.getRequiredActions())
        .containsExactly(KeycloakRequiredAction.VERIFY_EMAIL.toString());
  }

  @Test
  void toProfile_ShouldCallMapperAndReturnUnchangedResult() {
    // GIVEN
    String username = "testuser";
    PersistableCustomer persistableCustomer = new PersistableCustomer();
    persistableCustomer.setUsername(username);

    UUID userId = UUID.randomUUID();
    CustomerProfile mockProfile = new CustomerProfile();
    mockProfile.setUsername(username);
    mockProfile.setUserId(userId);

    given(customerMapper.toProfile(persistableCustomer)).willReturn(mockProfile);

    // WHEN
    CustomerProfile result = populator.toProfile(persistableCustomer);

    // THEN
    // Verify values haven't changed
    assertThat(result.getUserId()).isEqualTo(userId);
    assertThat(result.getUsername()).isEqualTo(username);
  }

  @Test
  void patchUpdate_ShouldDelegateToMapper() {
    // GIVEN
    UpdateCustomerProfileRequest updateRequest = new UpdateCustomerProfileRequest();
    CustomerProfile existingProfile = new CustomerProfile();
    existingProfile.setUserId(UUID.randomUUID());

    // We simulate the MapStruct behavior (returning the same object)
    given(customerMapper.patchUpdate(updateRequest, existingProfile)).willReturn(existingProfile);

    // WHEN
    CustomerProfile result = populator.patchUpdate(updateRequest, existingProfile);

    // THEN
    assertThat(result).isSameAs(existingProfile); // Identity check: nothing was lost
    verify(customerMapper).patchUpdate(updateRequest, existingProfile); // Interaction check
  }

  @Test
  void toCreatedUser_ShouldMapCorrectlyWithUuidConversion() {
    // GIVEN
    String validUuid = UUID.randomUUID().toString();
    UserRepresentation userRep = new UserRepresentation();
    userRep.setId(validUuid);

    CreatedUserResponse mockResponse = new CreatedUserResponse();
    given(customerMapper.toCreatedUser(userRep)).willReturn(mockResponse);

    // WHEN
    CreatedUserResponse result = populator.toCreatedUser(userRep);

    // THEN
    assertThat(result.getId()).isEqualTo(UUID.fromString(validUuid));
  }

  @Test
  void toProfileResponse_shouldMapToProfileResponseWithAvatarUrls() {
    // GIVEN
    CustomerProfile profile = createTestProfile();
    CustomerProfileResponse mockProfileResp = new CustomerProfileResponse();
    mockProfileResp.setUserId(profile.getUserId());

    // Mock the base mapper call
    given(customerMapper.toProfileResponse(profile)).willReturn(mockProfileResp);

    // Mock the URL converter calls for original, medium, and thumbnail
    given(urlConverter.media("my-bucket", "orig.jpg")).willReturn("http://cdn/orig.jpg");
    given(urlConverter.media("my-bucket", "med.jpg")).willReturn("http://cdn/med.jpg");
    given(urlConverter.media("my-bucket", "thumb.jpg")).willReturn("http://cdn/thumb.jpg");

    // WHEN
    CustomerProfileResponse result = populator.toProfileResponse(profile);

    // THEN
    // Verify AvatarResponse mapping
    AvatarResponse avatarResult = result.getAvatarMeta();

    assertThat(avatarResult.originalAvatarUrl()).isEqualTo("http://cdn/orig.jpg");
    assertThat(avatarResult.mediumAvatarUrl()).isEqualTo("http://cdn/med.jpg");
    assertThat(avatarResult.thumbnailAvatarUrl()).isEqualTo("http://cdn/thumb.jpg");
  }

  @Test
  void toProfileResponse_ShouldHandleNullAvatarGracefully() {
    // GIVEN
    CustomerProfile profile = new CustomerProfile();
    profile.setAvatarMeta(null);
    given(customerMapper.toProfileResponse(profile)).willReturn(new CustomerProfileResponse());

    // WHEN
    CustomerProfileResponse result = populator.toProfileResponse(profile);

    // THEN
    assertThat(result.getAvatarMeta()).isNull();
  }

  @Test
  void toPublicProfileResponse_ShouldMapToPublicProfileResponseWithAvatarUrls() {
    // GIVEN
    CustomerProfile profile = new CustomerProfile();
    profile.setUserId(UUID.randomUUID());

    AvatarMeta meta = new AvatarMeta("bucket-x", "origin.png", "med.png", "thumb.png");
    profile.setAvatarMeta(meta);

    PublicCustomerProfileResponse mockResponse = new PublicCustomerProfileResponse();
    given(customerMapper.toPublicProfileResponse(profile)).willReturn(mockResponse);

    // Mocking the 3 URL conversions inside toAvatarResponse
    given(urlConverter.media("bucket-x", "origin.png")).willReturn("https://cdn/origin.png");
    given(urlConverter.media("bucket-x", "med.png")).willReturn("https://cdn/med.png");
    given(urlConverter.media("bucket-x", "thumb.png")).willReturn("https://cdn/thumb.png");

    // WHEN
    PublicCustomerProfileResponse result = populator.toPublicProfileResponse(profile);

    // THEN
    AvatarResponse avatarResult = result.getAvatarMeta();

    assertThat(avatarResult.originalAvatarUrl()).isEqualTo("https://cdn/origin.png");
    assertThat(avatarResult.mediumAvatarUrl()).isEqualTo("https://cdn/med.png");
    assertThat(avatarResult.thumbnailAvatarUrl()).isEqualTo("https://cdn/thumb.png");
  }
}
