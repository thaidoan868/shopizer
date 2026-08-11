package vn.io.oldmoon.shopizer.user.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.user.infra.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByKeycloakUserId(UUID keycloakUserId);
}
