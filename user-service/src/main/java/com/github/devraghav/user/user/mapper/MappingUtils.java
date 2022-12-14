package com.github.devraghav.user.user.mapper;

import java.util.UUID;

public final class MappingUtils {

  public static final String GENERATE_UUID_EXPRESSION =
      "java(com.github.devraghav.user.user.mapper.MappingUtils.generateUuid())";

  public static String generateUuid() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
