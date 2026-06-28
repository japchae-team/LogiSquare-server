package com.example.logisquare_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logiSquareOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogiSquare API")
                        .description("LogiSquare server API documentation")
                        .version("v1"));
    }
}
