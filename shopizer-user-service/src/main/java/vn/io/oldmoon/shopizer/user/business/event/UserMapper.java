package vn.io.oldmoon.shopizer.user.business.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import vn.io.oldmoon.shopizer.user.infra.model.profile.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  @Mapping(target = "keycloakUserId", source = "eventDto.userId")
  @Mapping(target = "username", source = "eventDto.details.username")
  @Mapping(target = "email", source = "eventDto.details.email")
  @Mapping(target = "firstName", source = "eventDto.details.firstName")
  @Mapping(target = "lastName", source = "eventDto.details.lastName")
  // Ignore non-mapped fields that defaults will handle or are not in the payload
  @Mapping(target = "realm", ignore = true) // Defaulted to "shopizer" in entity
  @Mapping(target = "verified", ignore = true) // Defaulted to false in entity
  @Mapping(target = "avatarMeta", ignore = true)
  User toUserEntity(KeycloakUserRegisterEvent eventDto);
}
