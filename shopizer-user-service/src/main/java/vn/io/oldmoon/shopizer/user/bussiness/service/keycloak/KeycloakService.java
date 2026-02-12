package vn.io.oldmoon.shopizer.user.bussiness.service.keycloak;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.bussiness.exception.BusinessException;
import vn.io.oldmoon.shopizer.user.bussiness.exception.ErrorCode;
import vn.io.oldmoon.shopizer.user.bussiness.exception.KeycloakException;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {
    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;

    public UserRepresentation getUser(UUID userId) {
        String id = userId.toString();
        UsersResource users = keycloak.realm(realm).users();
        UserResource user = users.get(id);
        UserRepresentation userRep;

        try {
            userRep = user.toRepresentation();
        } catch (NotFoundException e) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND,
                    "User with id '%s' not found".formatted(id)
            );
        }
        return userRep;
    }

    public String create(UserRepresentation registerUser) {
        UsersResource usersResource = keycloak.realm(realm).users();

        // creation
        Response userResponse = usersResource.create(registerUser);

        int status = userResponse.getStatus();
        String body = null;

        if (status != 201 && userResponse.hasEntity()) {
            body = userResponse.readEntity(String.class);
        }

        if (status == 409) {
            log.info(
                    "Failed to create a new account (conflict). username={}, email={}, kcBody={}",
                    registerUser.getUsername(),
                    registerUser.getEmail(),
                    body
            );

            KeycloakErrorResponse errorResponse = userResponse.readEntity(KeycloakErrorResponse.class); // parse safely
            throw new BusinessException(ErrorCode.CONFLICT, errorResponse.getErrorMessage());
        }

        if (status != 201) {
            String locationPath = userResponse.getLocation() != null
                    ? userResponse.getLocation().getPath()
                    : null;

            log.error(
                    "Keycloak failed to create a new account. status={}, username={}, email={}, locationPath={}, kcBody={}",
                    status,
                    registerUser.getUsername(),
                    registerUser.getEmail(),
                    locationPath,
                    body
            );

            throw new KeycloakException(locationPath, body, status);
        }

        String userId = CreatedResponseUtil.getCreatedId(userResponse);
        log.info("Keycloak created a user with id={}", userId);
        return userId;
    }

    public void resetPassword(String userId, String newPassword) {
        UsersResource usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(userId);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(newPassword);
        passwordCred.setTemporary(false);

        userResource.resetPassword(passwordCred);

        userResource.logout();

        log.info("Password reset and sessions invalidated for userId={}", userId);
    }

    // public assignRole
}
