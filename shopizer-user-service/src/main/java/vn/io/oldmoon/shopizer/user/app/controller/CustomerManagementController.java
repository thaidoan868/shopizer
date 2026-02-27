package vn.io.oldmoon.shopizer.user.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUserResponse;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.app.facade.CustomerFacade;
import vn.io.oldmoon.shopizer.user.infra.data.constant.Language;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management Endpoints")
public class CustomerManagementController {
    private final CustomerFacade customerFacade;

    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<CreatedUserResponse> customerRegister(
            @Valid @RequestBody PersistableCustomer persistableCustomer,
            Locale locale
    ) {
        // set default language
        if (persistableCustomer.getLanguage() == null) {
            Language language;
            try {
                language = Language.valueOf(locale.getLanguage().toLowerCase());
            } catch (IllegalArgumentException e) {
                language = Language.en;
            }
        }

        CreatedUserResponse createdUserResponse = customerFacade.registerCustomer(persistableCustomer);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserResponse);
    }

    // @GetMapping("/{id}");

    // @PatchMapping("/{id}/profile/update");

    // @PatchMapping("/{id}/profile/avatar/update");
}