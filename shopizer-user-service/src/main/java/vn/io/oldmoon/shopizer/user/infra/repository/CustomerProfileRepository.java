package vn.io.oldmoon.shopizer.user.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.io.oldmoon.shopizer.user.infra.model.profile.CustomerProfile;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
  @Query(
      """
        SELECT new vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileQueryDto(
            cp.id
        )
        FROM CustomerProfile cp
        WHERE cp.user.keycloakUserId = :keycloakUserId
        """)
  Optional<CustomerProfileQueryDto> findByKeycloakUserId(
      @Param("keycloakUserId") UUID keycloakUserId);
}
