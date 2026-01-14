package com.leyuz.bbs.config;

import com.leyuz.bbs.filter.AccessLogFilter;
import com.leyuz.bbs.system.access.AccessLogApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 访问日志过滤器配置
 *
 * @author Walker
 * @since 2025-01-11
 */
@Configuration
public class AccessLogFilterConfig {

    /**
     * 注册访问日志过滤器
     * 在RequestHeaderFilter之后执行
     */
    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilter(
            AccessLogApplication accessLogApplication) {
        FilterRegistrationBean<AccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessLogFilter(accessLogApplication));
        registration.addUrlPatterns("/*");
        registration.setOrder(2);  // 在RequestHeaderFilter之后
        registration.setName("accessLogFilter");
        return registration;
    }
}
