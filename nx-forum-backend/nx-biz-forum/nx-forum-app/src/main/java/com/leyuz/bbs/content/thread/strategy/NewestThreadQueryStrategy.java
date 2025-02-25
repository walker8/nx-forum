package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 最新帖子查询策略
 *
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order(40) // 设置较高优先级
public class NewestThreadQueryStrategy extends AbstractThreadQueryStrategy {

    public NewestThreadQueryStrategy(ThreadMapper threadService, UserApplication userApplication) {
        super(threadService, userApplication);
    }

    @Override
    public boolean supports(String forumName) {
        return "newest".equals(forumName);
    }

    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        threadQuery.setOrderBy("create_time");
        threadQuery.setDays(180);
        return getThreadPOPage(0, threadQuery);
    }
} 