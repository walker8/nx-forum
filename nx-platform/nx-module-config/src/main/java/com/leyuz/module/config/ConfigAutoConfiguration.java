package com.leyuz.module.config;

import com.leyuz.module.config.app.CacheConfig;
import com.leyuz.module.config.app.ConfigApplication;
import com.leyuz.module.config.infrastructure.mybatis.ConfigServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 配置模块自动配置类
 */
@Configuration
@Import({
        CacheConfig.class,
        ConfigApplication.class,
        ConfigServiceImpl.class
})
@MapperScan("com.leyuz.module.config.infrastructure.mybatis.mapper")
public class ConfigAutoConfiguration {
    // 自动配置入口类
} 