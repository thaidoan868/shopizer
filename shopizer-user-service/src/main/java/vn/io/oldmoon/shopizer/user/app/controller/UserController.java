package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.io.oldmoon.shopizer.common.core.util.ImageUtil;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.user.app.dto.user.AvatarDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserDto;
import vn.io.oldmoon.shopizer.user.app.dto.user.UserPopulator;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;

@RestController
@RequestMapping(path = {"/api/v1/user", "/api/v1/users"})
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Endpoints")
public class UserController extends AbstractController {

  private final UserService userService;
  private final UserPopulator userPopulator;

  @PatchMapping(
      value = {"/avatar", "/me/avatar"},
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload user avatar",
      description = "Uploads and updates the avatar image for the authenticated user")
  @ApiResponse(
      responseCode = "200",
      description = "Avatar updated successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = AvatarDto.class)))
  public ResponseEntity<AvatarDto> updateAvatar(
      @RequestParam(value = "avatar", required = true) MultipartFile avatarFile) {
    ImageUtil.validateBasic(avatarFile);

    UUID keycloakUserId = getCurrentUserId();
    User updatedUser = userService.updateAvatar(keycloakUserId, avatarFile);
    UserDto updatedUserDto = userPopulator.toUserDto(updatedUser);
    return ResponseEntity.ok(updatedUserDto);
  }
}
