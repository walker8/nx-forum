package com.leyuz.ratelimit.config;

import com.leyuz.ratelimit.aspect.RateLimiterAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterAutoConfiguration {

    @Bean
    public RateLimiterAspect rateLimiterAspect() {
        return new RateLimiterAspect();
    }
} 