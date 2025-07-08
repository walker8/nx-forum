package com.leyuz.uc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 用户中心基础设施层自动配置类
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.uc"})
@MapperScan({"com.leyuz.uc.**.mybatis.mapper"})
public class UcInfrastructureAutoConfiguration {
    // 自动配置入口类
} 