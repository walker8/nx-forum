package com.leyuz.uc;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 用户中心适配层自动配置类
 */
@Configuration
@ComponentScan(basePackages = {"com.leyuz.uc"})
@MapperScan(value = "com.leyuz.uc", annotationClass = Mapper.class)
public class UcAdapterAutoConfiguration {
    // 自动配置入口类
} 