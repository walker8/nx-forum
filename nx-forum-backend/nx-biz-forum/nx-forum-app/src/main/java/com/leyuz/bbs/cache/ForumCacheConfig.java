package com.leyuz.bbs.cache;

import com.alicp.jetcache.anno.CacheType;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.forum.dto.ForumMenuItemVO;
import com.leyuz.bbs.forum.dto.ForumMenuVO;
import com.leyuz.module.cache.CacheService;
import com.leyuz.module.cache.GenericCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ForumCacheConfig {
    @Value("${nx.stats.enabled:false}")
    private Boolean stats;

    /**
     * 一些共用的缓存
     *
     * @param cacheService
     * @return
     */
    @Bean
    public GenericCache<String, String> commonCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "common:", 60 * 60 * 24L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<Integer, ForumPO> forumIdCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "forum:id:", 60 * 60 * 24L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, ForumPO> forumNameCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "forum:name:", 60 * 60 * 24L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, List<ForumMenuItemVO>> forumMenuItemCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "forum:menuItem:", 60 * 60 * 24L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, ForumMenuVO> forumMenuCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "forum:menu:", 60 * 60 * 24L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, Boolean> threadViewCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "thread:id:", 60 * 60 * 2L, CacheType.REMOTE);
    }

    @Bean
    public GenericCache<String, List<ThreadVO>> threadsHotCache(CacheService cacheService) {
        return new GenericCache<>(cacheService, stats, "threads:hot:", 60 * 60 * 2L, CacheType.REMOTE);
    }
}
