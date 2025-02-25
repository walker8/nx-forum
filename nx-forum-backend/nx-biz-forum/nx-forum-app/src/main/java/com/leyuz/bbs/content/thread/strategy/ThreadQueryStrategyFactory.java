package com.leyuz.bbs.content.thread.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子查询策略工厂
 *
 * @author walker
 * @since 2025-03-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ThreadQueryStrategyFactory {

    private final List<ThreadQueryStrategy> strategies;

    /**
     * 获取查询策略
     *
     * @param forumName 论坛名称
     * @return 查询策略
     */
    public ThreadQueryStrategy getStrategy(String forumName) {
        // 按照排序后的顺序查找匹配的策略
        return strategies.stream()
                .filter(strategy -> strategy.supports(forumName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的查询类型: " + forumName));
    }
} 