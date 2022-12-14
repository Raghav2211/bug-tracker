package com.github.devraghav.user.user.mapper;

import com.github.devraghav.user.user.dto.AccessLevel;
import com.github.devraghav.user.user.dto.User;
import com.github.devraghav.user.user.dto.UserException;
import com.github.devraghav.user.user.dto.UserRequest;
import com.github.devraghav.user.user.entity.UserEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mappings({
    @Mapping(target = "id", expression = MappingUtils.GENERATE_UUID_EXPRESSION),
    @Mapping(target = "enabled", constant = "true"),
    @Mapping(target = "access", source = "access", qualifiedByName = "accessLevelToValue")
  })
  UserEntity requestToEntity(UserRequest userRequest);

  @Mappings({
    @Mapping(target = "access", source = "access", qualifiedByName = "valueToAccessLevel")
  })
  User entityToResponse(UserEntity todo);

  @Named("accessLevelToValue")
  default Integer accessLevelToValue(AccessLevel accessLevel) {
    return switch (accessLevel) {
      case READ -> AccessLevel.READ.getValue();
      case WRITE -> AccessLevel.WRITE.getValue();
      case ADMIN -> AccessLevel.ADMIN.getValue();
    };
  }

  @Named("valueToAccessLevel")
  default AccessLevel valueToAccessLevel(Integer accessLevelValue) {
    return switch (accessLevelValue) {
      case 1 -> AccessLevel.READ;
      case 2 -> AccessLevel.WRITE;
      case 0 -> AccessLevel.ADMIN;
      default -> throw UserException.unrecognizedAccessLevel();
    };
  }
}
