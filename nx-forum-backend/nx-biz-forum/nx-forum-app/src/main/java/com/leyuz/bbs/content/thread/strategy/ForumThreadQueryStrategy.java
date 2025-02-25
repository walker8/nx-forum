package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 特定论坛的查询策略
 * 
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order(60) // 设置较高优先级
public class ForumThreadQueryStrategy extends AbstractThreadQueryStrategy {
    
    public ForumThreadQueryStrategy(ThreadMapper threadMapper, UserApplication userApplication) {
        super(threadMapper, userApplication);
    }
    
    @Override
    public boolean supports(String forumName) {
        // 这个策略不通过名称匹配，而是在 ThreadApplication 中直接调用
        return false;
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        return getThreadPOPage(threadQuery.getForumId(), threadQuery);
    }
    
    /**
     * 查询帖子（不包含置顶帖子）
     * 用于关键词搜索等场景，避免全局置顶帖子干扰搜索结果
     * 
     * @param threadQuery 查询参数
     * @return 查询结果
     */
    public Page<ThreadPO> queryWithoutTop(ThreadQuery threadQuery) {
        return getThreadPOPageWithoutTop(threadQuery.getForumId(), threadQuery);
    }
} 