package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 默认帖子查询策略
 *
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order
public class DefaultThreadQueryStrategy extends AbstractThreadQueryStrategy {

    public DefaultThreadQueryStrategy(ThreadMapper threadService, UserApplication userApplication) {
        super(threadService, userApplication);
    }

    @Override
    public boolean supports(String forumName) {
        // 默认策略支持所有未被其他策略处理的查询
        return true;
    }

    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        // 默认查询逻辑
        return getThreadPOPage(0, threadQuery);
    }
} 