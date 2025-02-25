package com.leyuz.module.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheService {
    @Autowired
    private CacheManager cacheManager;
    @Value("${nx.cache.redis.enabled:false}")
    private boolean redisEnabled;
    // 默认缓存时间
    private static final long DEFAULT_TIMEOUT = 3600;
    // 默认缓存名称
    private static final String DEFAULT_NAME = "default:";

    public Cache<Object, Object> create() {
        CacheType cacheType = CacheType.REMOTE;
        return create(DEFAULT_NAME, DEFAULT_TIMEOUT, cacheType);
    }

    public <K, V> Cache<K, V> create(String name, long timeout, CacheType cacheType) {
        if (!redisEnabled) {
            // 如果redis未启用，则使用本地缓存
            cacheType = CacheType.LOCAL;
        }
        if (StringUtils.isBlank(name)) {
            name = DEFAULT_NAME;
        }
        if (!name.endsWith(":")) {
            name = name + ":";
        }
        QuickConfig qc = QuickConfig.newBuilder(name)
                .expire(Duration.ofSeconds(timeout))
                .cacheType(cacheType)
                // 本地缓存更新后，将在所有的节点中删除缓存，以保持强一致性
                .syncLocal(true)
                .build();
        return cacheManager.getOrCreateCache(qc);
    }
} 