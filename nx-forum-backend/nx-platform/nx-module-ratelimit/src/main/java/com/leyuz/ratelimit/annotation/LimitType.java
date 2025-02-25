package com.leyuz.ratelimit.annotation;

/**
 * 限流类型
 */
public enum LimitType {
    /**
     * 默认策略全局限流
     */
    DEFAULT,

    /**
     * 根据IP进行限流
     */
    IP,

    /**
     * 根据用户ID限流
     */
    USER_ID
} 