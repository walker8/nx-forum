package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.uc.user.UserApplication;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class AllThreadQueryStrategy extends AbstractThreadQueryStrategy {

    public AllThreadQueryStrategy(ThreadMapper threadMapper, UserApplication userApplication) {
        super(threadMapper, userApplication);
    }

    @Override
    public boolean supports(String forumName) {
        return "all".equals(forumName);
    }

    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        threadQuery.setOrderBy("last_comment_time");
        return getThreadPOPage(0, threadQuery);
    }
}
