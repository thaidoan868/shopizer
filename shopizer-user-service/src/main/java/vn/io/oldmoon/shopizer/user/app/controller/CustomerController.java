package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.common.core.exception.UnauthorizedActionException;
import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerPopulator;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CustomerProfileDto;
import vn.io.oldmoon.shopizer.user.business.service.UserService;
import vn.io.oldmoon.shopizer.user.business.service.profile.CustomerProfileService;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

@RestController
@RequestMapping({"/api/v1/customer", "/api/v1/users/customers"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Endpoints")
public class CustomerController extends AbstractController {

  private final UserService userService;
  private final CustomerProfileService customerProfileService;
  private final CustomerPopulator customerPopulator;

  @GetMapping("/profile")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get current customer profile details")
  public ResponseEntity<CustomerProfileDto> getProfile() {
    UUID keycloakUserId =
        getCurrentUserId()
            .orElseThrow(() -> new UnauthorizedActionException("User is not authenticated"));

    User user = userService.get(keycloakUserId);
    CustomerProfile profile = customerProfileService.get(keycloakUserId);
    CustomerProfileDto profileDto = customerPopulator.toCustomerProfileDto(user, profile);
    return ResponseEntity.ok(profileDto);
  }
}
