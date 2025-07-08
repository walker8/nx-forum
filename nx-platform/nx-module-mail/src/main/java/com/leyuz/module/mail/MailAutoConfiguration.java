package com.leyuz.module.mail;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件模块自动配置类
 * 启用邮件模块的组件扫描
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.module.mail"})
public class MailAutoConfiguration {
    // 自动配置入口类
} 