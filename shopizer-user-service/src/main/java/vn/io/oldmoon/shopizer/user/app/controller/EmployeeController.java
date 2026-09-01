package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.user.app.dto.employee.EmployeePopulator;
import vn.io.oldmoon.shopizer.user.app.dto.employee.EmployeeProfileDto;
import vn.io.oldmoon.shopizer.user.app.dto.employee.UpdateEmployeeDto;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.EmployeeProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@RestController
@PreAuthorize(
    "hasAnyRole('STORE_MANAGER', 'SUPER_ADMIN', 'SUPPORT_AGENT', 'WAREHOUSE_STAFF')")
@RequestMapping("/api/v1/employees/me")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Endpoints")
public class EmployeeController extends AbstractController {

  private final UserService userService;
  private final EmployeeProfileService employeeProfileService;
  private final EmployeePopulator employeePopulator;

  @GetMapping("/profile")
  @Operation(
      summary = "Get current employee profile details",
      description = "Retrieves profile details for the authenticated employee")
  @ApiResponse(
      responseCode = "200",
      description = "Employee profile retrieved successfully",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EmployeeProfileDto.class)))
  public ResponseEntity<EmployeeProfileDto> getProfile() {
    UUID keycloakUserId = getCurrentUserId();

    User user = userService.get(keycloakUserId);
    EmployeeProfile profile = employeeProfileService.get(keycloakUserId);
    EmployeeProfileDto profileDto = employeePopulator.toEmployeeProfileDto(user, profile);
    return ResponseEntity.ok(profileDto);
  }

  @PatchMapping("/profile")
  @Operation(
      summary = "Update current employee profile details",
      description = "Updates profile information for the authenticated employee")
  @ApiResponse(
      responseCode = "200",
      description = "Profile updated successfully",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = EmployeeProfileDto.class)))
  public ResponseEntity<EmployeeProfileDto> updateProfile(
      @Valid @RequestBody UpdateEmployeeDto updateEmployeeDto) {
    UUID keycloakUserId = getCurrentUserId();
    User user = userService.get(keycloakUserId);
    EmployeeProfile profile = employeeProfileService.get(keycloakUserId);

    employeePopulator.update(user, profile, updateEmployeeDto);

    EmployeeProfile updatedProfile = employeeProfileService.update(user, profile);
    EmployeeProfileDto profileDto =
        employeePopulator.toEmployeeProfileDto(updatedProfile.getUser(), updatedProfile);
    return ResponseEntity.ok(profileDto);
  }
}
