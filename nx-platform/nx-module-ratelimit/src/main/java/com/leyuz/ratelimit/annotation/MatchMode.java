package com.leyuz.ratelimit.annotation;

/**
 * 规则匹配模式
 */
public enum MatchMode {
    /**
     * 所有规则都通过才允许访问
     */
    AND,
    
    /**
     * 任一规则通过即允许访问
     */
    OR
} 