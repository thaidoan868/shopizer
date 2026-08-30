package vn.io.oldmoon.shopizer.user.infra.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
  @Query(
      """
        SELECT new vn.io.oldmoon.shopizer.user.infra.repository.EmployeeProfileQueryDto(
            ep.id
        )
        FROM EmployeeProfile ep
        WHERE ep.user.keycloakUserId = :keycloakUserId
        """)
  Optional<EmployeeProfileQueryDto> findByKeycloakUserId(
      @Param("keycloakUserId") UUID keycloakUserId);
}
