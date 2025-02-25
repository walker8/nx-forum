package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 推荐帖子查询策略
 * 
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order(10) // 设置较高优先级
public class RecommendThreadQueryStrategy extends AbstractThreadQueryStrategy {
    
    public RecommendThreadQueryStrategy(ThreadMapper threadMapper, UserApplication userApplication) {
        super(threadMapper, userApplication);
    }
    
    @Override
    public boolean supports(String forumName) {
        return "recommend".equals(forumName);
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        threadQuery.setPropertyTypeV(ThreadPropertyTypeV.RECOMMEND);
        return getThreadPOPage(0, threadQuery);
    }
} 