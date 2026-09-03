package vn.io.oldmoon.shopizer.user.app.dto.employee;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.io.oldmoon.shopizer.user.infra.model.user.User;
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

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "realm", ignore = true)
  @Mapping(target = "keycloakUserId", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "verified", ignore = true)
  @Mapping(target = "avatarMeta", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updateUserFromDto(UpdateEmployeeDto dto, @MappingTarget User user);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "created", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "modified", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  void updateEmployeeProfileFromDto(
      UpdateEmployeeDto dto, @MappingTarget EmployeeProfile employeeProfile);
}
