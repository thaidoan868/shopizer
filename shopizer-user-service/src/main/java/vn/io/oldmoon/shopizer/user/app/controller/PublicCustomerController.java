package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.user.app.facade.CustomerFacade;
import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.PublicCustomerProfileResponse;

@RestController
@RequestMapping("/api/v1/public/users/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management Endpoints")
public class PublicCustomerController {
  private final CustomerFacade customerFacade;

  @GetMapping("/{userId}/profile")
  @Operation(summary = "Get public profile information")
  public ResponseEntity<PublicCustomerProfileResponse> getPublicProfile(
      @PathVariable("userId") UUID userId) {
    PublicCustomerProfileResponse response = customerFacade.getPublicProfile(userId);
    return ResponseEntity.ok(response);
  }
}
