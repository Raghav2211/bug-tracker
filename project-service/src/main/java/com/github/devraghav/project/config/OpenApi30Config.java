package com.github.devraghav.project.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Issue tracker app",
            version = "1.0.0",
            description = "Issue tracker WebFlux Rest API"))
public class OpenApi30Config {}
