package vn.io.oldmoon.shopizer.user.app.populator.user;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import vn.io.oldmoon.shopizer.user.app.dto.customer.CreatedUser;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPopulator {
    private final UserMapper userMapper;

    public CreatedUser toCreatedUser(UserRepresentation user) {
        CreatedUser createdUser = userMapper.toCreatedUser(user);
        String id = user.getId();
        if (id != null && !id.isBlank()) {
            createdUser.setId(UUID.fromString(user.getId()));
        }
        return createdUser;
    }
}