package com.leyuz.bbs.cache;

import com.alicp.jetcache.anno.CacheType;
import com.leyuz.module.cache.CacheService;
import com.leyuz.module.cache.GenericCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 访问日志缓存配置
 *
 * @author Walker
 * @since 2025-01-11
 */
@Configuration
public class AccessLogCacheConfig {

    /**
     * 访问日志去重缓存
     * Key: access_log:{ip}:{deviceId}
     * Value: "1" (占位符)
     * TTL: 20分钟
     */
    @Bean
    public GenericCache<String, String> accessLogCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, false, "access_log:", 1200L, CacheType.BOTH);
    }
}
