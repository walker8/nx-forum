package com.leyuz.bbs.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.bbs.thread.dto.ThreadQuery;
import com.leyuz.bbs.thread.mybatis.IThreadService;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 后台管理的查询策略
 *
 * @author walker
 * @since 2025-03-16
 */
@Component
@Order(60) // 设置较高优先级
public class AdminThreadQueryStrategy extends AbstractThreadQueryStrategy {

    public AdminThreadQueryStrategy(IThreadService threadService, UserApplication userApplication) {
        super(threadService, userApplication);
    }

    @Override
    public boolean supports(String forumName) {
        // 这个策略不通过名称匹配，而是在 ThreadApplication 中直接调用
        return "admin".equals(forumName);
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        return queryThreads(threadQuery.getForumId(), threadQuery);
    }
} 