package vn.io.oldmoon.shopizer.user.infra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    Optional<CustomerProfile> findByUserId(UUID userId);
}
