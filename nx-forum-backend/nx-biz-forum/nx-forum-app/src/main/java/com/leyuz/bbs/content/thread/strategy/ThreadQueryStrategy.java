package com.leyuz.bbs.content.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;

/**
 * 帖子查询策略接口
 * 
 * @author walker
 * @since 2025-03-01
 */
public interface ThreadQueryStrategy {
    
    /**
     * 判断是否支持该查询类型
     * 
     * @param forumName 论坛名称
     * @return 是否支持
     */
    boolean supports(String forumName);
    
    /**
     * 执行查询
     * 
     * @param threadQuery 查询参数
     * @return 查询结果
     */
    Page<ThreadPO> query(ThreadQuery threadQuery);
} 