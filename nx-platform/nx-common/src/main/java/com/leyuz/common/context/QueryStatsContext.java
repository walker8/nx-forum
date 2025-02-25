package com.leyuz.common.context;

import lombok.Data;

/**
 * 查询统计上下文
 */
public class QueryStatsContext {
    private static final ThreadLocal<QueryStats> statsHolder = ThreadLocal.withInitial(QueryStats::new);

    public static void reset() {
        statsHolder.remove();
    }

    public static QueryStats get() {
        return statsHolder.get();
    }

    public static void incrementDbQuery() {
        get().setDbQueryCount(get().getDbQueryCount() + 1);
    }

    public static void incrementCacheHit() {
        get().setCacheHits(get().getCacheHits() + 1);
    }

    public static void incrementCacheMiss() {
        get().setCacheMisses(get().getCacheMisses() + 1);
    }

    @Data
    public static class QueryStats {
        // 数据库查询次数
        private int dbQueryCount;
        // 缓存命中次数
        private int cacheHits;
        // 缓存未命中次数
        private int cacheMisses;
    }
} 