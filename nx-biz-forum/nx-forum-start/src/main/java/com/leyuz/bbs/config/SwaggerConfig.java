package com.leyuz.bbs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NX Forum API")
                        .description("NX Forum 接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Walker")
                                .email("nicholas199109@gmail.com")));
    }
} 