package com.leyuz.module.sms;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 短信模块自动配置类
 * 启用短信模块的组件扫描
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.module.sms"})
public class SmsAutoConfiguration {
    // 自动配置入口类
} 