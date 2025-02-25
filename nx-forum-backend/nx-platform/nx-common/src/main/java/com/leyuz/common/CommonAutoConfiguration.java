package com.leyuz.common;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 公共模块自动配置类
 * 启用公共模块的组件扫描
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.common"})
public class CommonAutoConfiguration {
    // 自动配置入口类
} 