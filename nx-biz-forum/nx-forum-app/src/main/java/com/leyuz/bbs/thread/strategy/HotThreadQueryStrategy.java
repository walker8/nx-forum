package com.leyuz.bbs.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.bbs.thread.dto.ThreadQuery;
import com.leyuz.bbs.thread.mybatis.IThreadService;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 热门帖子查询策略
 * 
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order(30) // 设置较高优先级
public class HotThreadQueryStrategy extends AbstractThreadQueryStrategy {
    
    public HotThreadQueryStrategy(IThreadService threadService, UserApplication userApplication) {
        super(threadService, userApplication);
    }
    
    @Override
    public boolean supports(String forumName) {
        return "hot".equals(forumName);
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        threadQuery.setOrderBy("views");
        threadQuery.setDays(30);
        return getThreadPOPage(0, threadQuery);
    }
} 