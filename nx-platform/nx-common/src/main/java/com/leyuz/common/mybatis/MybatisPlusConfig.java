package com.leyuz.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MybatisPlusConfig {
    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * SQL统计插件（保持条件加载）
     */
    @Bean
    @ConditionalOnProperty(name = "stats.enabled", havingValue = "true")
    public QueryStatsInterceptor queryStatsInterceptor() {
        return new QueryStatsInterceptor();
    }

    /**
     * 统一配置SqlSessionFactory
     */
    @Bean
    @ConditionalOnProperty(name = "stats.enabled", havingValue = "true")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 分页插件
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(mybatisPlusInterceptor());

        // 添加统计插件
        interceptors.add(queryStatsInterceptor());

        sessionFactory.setPlugins(interceptors.toArray(new Interceptor[0]));
        return sessionFactory.getObject();
    }
}
