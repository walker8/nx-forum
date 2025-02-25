package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 归档帖子查询策略
 * 
 * @author walker
 * @since 2025-11-25
 */
@Component
@Order(50) // 设置较高优先级
public class ArchiveThreadQueryStrategy extends AbstractThreadQueryStrategy {
    
    public ArchiveThreadQueryStrategy(ThreadMapper threadMapper, UserApplication userApplication) {
        super(threadMapper, userApplication);
    }
    
    @Override
    public boolean supports(String forumName) {
        return "archive".equals(forumName);
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        // 查询归档版块(forum_id=6)的主题
        threadQuery.setOrderBy("create_time");
        return getThreadPOPage(6, threadQuery);
    }
}
