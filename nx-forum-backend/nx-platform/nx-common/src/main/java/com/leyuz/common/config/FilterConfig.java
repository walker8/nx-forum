package com.leyuz.common.config;

import com.leyuz.common.filter.QueryStatsFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    @ConditionalOnProperty(name = "nx.stats.enabled", havingValue = "true")
    public FilterRegistrationBean<QueryStatsFilter> queryStatsFilter() {
        FilterRegistrationBean<QueryStatsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new QueryStatsFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // 设置合适的过滤顺序
        return registration;
    }
}