package com.leyuz.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
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
    @ConditionalOnProperty(name = "nx.stats.enabled", havingValue = "true")
    public QueryStatsInterceptor queryStatsInterceptor() {
        return new QueryStatsInterceptor();
    }

    /**
     * 统一配置自定义SqlSessionFactory
     * 仅当启用统计插件且没有其他sqlSessionFactory时生效
     */
    @Bean(name = "customSqlSessionFactory")
    @Primary
    @ConditionalOnProperty(name = "nx.stats.enabled", havingValue = "true")
    public SqlSessionFactory customSqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 配置 Mapper XML 文件位置
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:mapper/*.xml");
            sessionFactory.setMapperLocations(resources);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mapper XML files", e);
        }

        // 分页插件
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(mybatisPlusInterceptor());

        // 添加统计插件
        interceptors.add(queryStatsInterceptor());

        sessionFactory.setPlugins(interceptors.toArray(new Interceptor[0]));
        return sessionFactory.getObject();
    }
}
