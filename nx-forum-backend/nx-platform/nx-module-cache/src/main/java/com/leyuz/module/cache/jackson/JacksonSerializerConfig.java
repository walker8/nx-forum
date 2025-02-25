package com.leyuz.module.cache.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonSerializerConfig {
    public JacksonSerializerConfig() {
    }

    @Bean
    public JacksonEncoderParser jacksonEncoderParser() {
        return new JacksonEncoderParser();
    }
}
