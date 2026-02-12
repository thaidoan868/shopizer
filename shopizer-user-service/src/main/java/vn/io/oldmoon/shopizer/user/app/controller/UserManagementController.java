package vn.io.oldmoon.shopizer.user.app.controller;

import io.micrometer.tracing.annotation.NewSpan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;
import vn.io.oldmoon.shopizer.user.app.dto.customer.PersistableCustomer;
import vn.io.oldmoon.shopizer.user.infra.constant.Language;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management Endpoints")
public class UserManagementController {

    @NewSpan
    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }

    @NewSpan
    @PostMapping("/customers/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<CreatedUser> customerRegister(
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

        return ResponseEntity.noContent().build();
    }
}