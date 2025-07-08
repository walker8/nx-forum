package com.leyuz.module.cache;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存模块自动配置类
 * 启用缓存模块的组件扫描
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.module.cache"})
public class CacheAutoConfiguration {
    // 自动配置入口类
} 