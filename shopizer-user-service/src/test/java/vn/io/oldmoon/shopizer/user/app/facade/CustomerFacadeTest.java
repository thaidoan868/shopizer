package vn.io.oldmoon.shopizer.user.app.facade;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.io.oldmoon.shopizer.common.core.exception.ApiException;
import vn.io.oldmoon.shopizer.common.core.exception.ErrorCode;
import vn.io.oldmoon.shopizer.common.core.exception.ResourceNotFoundException;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;
import vn.io.oldmoon.shopizer.user.app.transfer.populator.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.business.service.CustomerService;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

class CustomerFacadeTest {
  @Mock private CustomerPopulator customerPopulator;

  @Mock private CustomerService customerService;

  @InjectMocks private CustomerFacade customerFacade;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getProfile_shouldReturnProfileResponse_whenProfileExists() {
    UUID userId = UUID.randomUUID();
    CustomerProfile profile = new CustomerProfile();
    CustomerProfileResponse response = new CustomerProfileResponse();

    when(customerService.get(userId)).thenReturn(profile);
    when(customerPopulator.toProfileResponse(profile)).thenReturn(response);

    CustomerProfileResponse result = customerFacade.getProfile(userId);

    assertEquals(response, result);
    verify(customerService).get(userId);
    verify(customerPopulator).toProfileResponse(profile);
  }

  @Test
  void getProfile_shouldThrowApiException_whenProfileNotFound() {
    UUID userId = UUID.randomUUID();

    when(customerService.get(userId))
        .thenThrow(new ResourceNotFoundException("Profile", "userId" + userId));

    ApiException exception =
        assertThrows(ApiException.class, () -> customerFacade.getProfile(userId));

    assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    assertTrue(exception.getMessage().contains(userId.toString()));

    verify(customerService).get(userId);
    verifyNoInteractions(customerPopulator);
  }

  @Test
  void getPublicProfile_shouldReturnPublicResponse_whenProfileExists() {
    UUID userId = UUID.randomUUID();
    CustomerProfile profile = new CustomerProfile();
    PublicCustomerProfileResponse response = new PublicCustomerProfileResponse();

    when(customerService.get(userId)).thenReturn(profile);
    when(customerPopulator.toPublicProfileResponse(profile)).thenReturn(response);

    PublicCustomerProfileResponse result = customerFacade.getPublicProfile(userId);

    assertEquals(response, result);
    verify(customerService).get(userId);
    verify(customerPopulator).toPublicProfileResponse(profile);
  }

  @Test
  void getPublicProfile_shouldThrowApiException_whenProfileNotFound() {
    UUID userId = UUID.randomUUID();

    when(customerService.get(userId))
        .thenThrow(new ResourceNotFoundException("Profile", "userId" + userId));

    ApiException exception =
        assertThrows(ApiException.class, () -> customerFacade.getPublicProfile(userId));

    assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    assertTrue(exception.getMessage().contains(userId.toString()));

    verify(customerService).get(userId);
    verifyNoInteractions(customerPopulator);
  }
}
