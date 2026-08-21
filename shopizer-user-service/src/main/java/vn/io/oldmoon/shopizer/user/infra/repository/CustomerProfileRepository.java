package vn.io.oldmoon.shopizer.user.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
  Optional<CustomerProfile> findByKeycloakUserId(UUID userId);
}
