package com.leyuz.module.config.app;

import com.alicp.jetcache.anno.CacheType;
import com.leyuz.module.cache.CacheService;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.module.config.infrastructure.ConfigPO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration("configCacheConfig")
public class CacheConfig {
    @Value("${nx.stats.enabled:false}")
    private Boolean stats;

    @Bean
    public GenericCache<String, List<ConfigPO>> configCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "config:list:", 3600 * 24 * 7L, CacheType.REMOTE);
    }
}
