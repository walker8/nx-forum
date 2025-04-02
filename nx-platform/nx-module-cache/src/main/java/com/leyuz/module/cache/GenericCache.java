package com.leyuz.module.cache;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.leyuz.common.context.QueryStatsContext;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 泛型缓存
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class GenericCache<K, V> {
    private final CacheService cacheService;
    private final String keyPrefix;
    private final long expire;
    private final CacheType cacheType;
    private final Boolean stats;

    private Cache<K, V> cache;


    /**
     * 初始化缓存对象
     *
     * @param cacheService 缓存服务
     * @param stats        是否开启统计
     * @param keyPrefix    key前缀
     * @param expire       过期时间（秒）
     * @param cacheType    缓存类型
     */
    public GenericCache(CacheService cacheService, Boolean stats, String keyPrefix, long expire, CacheType cacheType) {
        this.cacheService = cacheService;
        this.keyPrefix = keyPrefix;
        this.expire = expire;
        this.cacheType = cacheType;
        this.stats = stats;
        init();
    }

    private void init() {
        // 显式指定泛型类型
        cache = cacheService.create(keyPrefix, expire, cacheType);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public void put(K key, V value, Long expireAfterWriter) {
        cache.put(key, value, expireAfterWriter, TimeUnit.SECONDS);
    }

    public void put(K key, V value, Long expireAfterWriter, TimeUnit timeUnit) {
        cache.put(key, value, expireAfterWriter, timeUnit);
    }

    public V get(K key) {
        V value = cache.get(key);
        if (Boolean.TRUE.equals(stats)) {
            if (value != null) {
                QueryStatsContext.incrementCacheHit();
            } else {
                QueryStatsContext.incrementCacheMiss();
            }
        }
        return value;
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public V computeIfAbsent(K key, Function<K, V> loader) {
        if (Boolean.TRUE.equals(stats)) {
            V value = cache.get(key);
            if (value != null) {
                QueryStatsContext.incrementCacheHit();
            } else {
                QueryStatsContext.incrementCacheMiss();
            }
        }
        return cache.computeIfAbsent(key, loader);
    }

    public V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expireAfterWrite, TimeUnit timeUnit) {
        if (Boolean.TRUE.equals(stats)) {
            V value = cache.get(key);
            if (value != null) {
                QueryStatsContext.incrementCacheHit();
            } else {
                QueryStatsContext.incrementCacheMiss();
            }
        }
        return cache.computeIfAbsent(key, loader, cacheNullWhenLoaderReturnNull, expireAfterWrite, timeUnit);
    }

    // 添加带默认值的查询方法
    public V getWithDefault(K key, V defaultValue) {
        V value = cache.get(key);
        return value != null ? value : defaultValue;
    }
}