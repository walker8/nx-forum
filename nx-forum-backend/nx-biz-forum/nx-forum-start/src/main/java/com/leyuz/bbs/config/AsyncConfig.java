package com.leyuz.bbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 *
 * @author Walker
 * @since 2025-01-11
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 访问日志异步执行器
     */
    @Bean("accessLogExecutor")
    public Executor accessLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("access-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 统计聚合异步执行器
     */
    @Bean("statsAggregationExecutor")
    public Executor statsAggregationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("stats-aggregate-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * AI 审核异步执行器
     *
     * <p>采用 AbortPolicy：队列满后直接抛 RejectedExecutionException，由 {@code ContentAiAuditListener}
     * 的 try/catch 兜底记日志并跳过本次复审（AI 厂商限流/宕机时，宁可漏审也不能把请求线程
     * 阻塞在事务提交线程上阻塞用户发主题/评论）。</p>
     */
    @Bean("aiAuditExecutor")
    public Executor aiAuditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-audit-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
