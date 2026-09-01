package vn.io.oldmoon.shopizer.user.app.dto.employee;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.user.infra.model.User;
import vn.io.oldmoon.shopizer.user.infra.model.profile.EmployeeProfile;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "keycloakUserId", source = "user.keycloakUserId")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "firstName", source = "user.firstName")
  @Mapping(target = "lastName", source = "user.lastName")
  @Mapping(target = "verified", source = "user.verified")
  @Mapping(target = "shift", source = "employeeProfile.shift")
  @Mapping(target = "workPhone", source = "employeeProfile.workPhone")
  @Mapping(target = "avatarMeta", ignore = true)
  EmployeeProfileDto toEmployeeProfileDto(User user, EmployeeProfile employeeProfile);
}
