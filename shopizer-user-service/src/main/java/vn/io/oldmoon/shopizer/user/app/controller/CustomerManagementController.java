// package vn.io.oldmoon.shopizer.user.app.controller;
//
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import vn.io.oldmoon.shopizer.common.web.controller.AbstractController;
// import vn.io.oldmoon.shopizer.user.app.facade.CustomerFacade;
// import vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.CustomerProfileResponse;
// import
// vn.io.oldmoon.shopizer.user.app.transfer.dto.customer.profile.UpdateCustomerProfileRequest;
//
// @RestController
// @RequestMapping("/api/v1/users/customers")
// @RequiredArgsConstructor
// @Slf4j
// @Tag(name = "Customer Management Endpoints")
// @Deprecated
// public class CustomerManagementController extends AbstractController {
//  private final CustomerFacade customerFacade;
//
//  @GetMapping("/me/profile")
//  @Operation(summary = "Get the full profile")
//  public ResponseEntity<CustomerProfileResponse> getProfile() {
//    CustomerProfileResponse response = customerFacade.getProfile(getCurrentUserId());
//    return ResponseEntity.ok(response);
//  }
//
//  @PatchMapping("/me/profile")
//  @Operation(summary = "Update partly the current user's profile")
//  public ResponseEntity<CustomerProfileResponse> updateProfile(
//      @Valid @RequestBody UpdateCustomerProfileRequest request) {
//    CustomerProfileResponse response = customerFacade.updateProfile(getCurrentUserId(), request);
//    return ResponseEntity.ok(response);
//  }
// }
