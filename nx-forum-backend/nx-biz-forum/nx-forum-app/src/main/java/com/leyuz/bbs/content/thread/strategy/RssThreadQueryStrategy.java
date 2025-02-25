package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@Order(10)
public class RssThreadQueryStrategy extends AbstractThreadQueryStrategy {

    public RssThreadQueryStrategy(ThreadMapper threadService, UserApplication userApplication) {
        super(threadService, userApplication);
    }

    @Override
    public boolean supports(String forumName) {
        return "rss".equals(forumName);
    }

    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        return getThreadPOPageWithoutTop(threadQuery.getForumId(), threadQuery);
    }
} 