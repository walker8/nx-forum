package com.leyuz.uc.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alicp.jetcache.anno.CacheType;
import com.leyuz.module.cache.CacheService;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.uc.auth.role.dto.RoleDTO;
import com.leyuz.uc.user.UserE;

@Configuration("ucCacheConfig")
public class CacheConfig {
    @Value("${nx.stats.enabled:false}")
    private Boolean stats;

    public static final String LOGIN_FAIL_COUNT_CACHE = "login:failCount";
    public static final String LOGIN_LOCK_CACHE = "login:lock";

    @Bean
    public GenericCache<String, Integer> loginFailCountCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, LOGIN_FAIL_COUNT_CACHE, 30 * 60L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, Boolean> loginLockCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, LOGIN_LOCK_CACHE, 30 * 60L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, String> smsCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "sms:verifyCode:", 300, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, String> mailCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "mail:verifyCode:", 1200, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<Long, UserE> userIdCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "user:id:", 3600, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<Long, String> emailChangeStepCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "email:change:", 900, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, Long> userTokenCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "user:token:", 5, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, List<RoleDTO>> roleListCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "user:role:", 3600, CacheType.REMOTE);
    }
}