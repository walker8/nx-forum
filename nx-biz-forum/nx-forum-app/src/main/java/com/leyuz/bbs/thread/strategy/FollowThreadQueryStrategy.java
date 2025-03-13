package com.leyuz.bbs.thread.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.thread.ThreadPO;
import com.leyuz.bbs.thread.dto.ThreadQuery;
import com.leyuz.bbs.thread.mybatis.IThreadService;
import com.leyuz.bbs.user.UserFollowApplication;
import com.leyuz.bbs.user.dto.UserFollowVO;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.user.UserApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注帖子查询策略
 * 
 * @author walker
 * @since 2025-03-01
 */
@Component
@Order(50) // 设置较高优先级
@Slf4j
public class FollowThreadQueryStrategy extends AbstractThreadQueryStrategy {
    
    private final UserFollowApplication userFollowApplication;
    
    public FollowThreadQueryStrategy(IThreadService threadService, UserApplication userApplication, UserFollowApplication userFollowApplication) {
        super(threadService, userApplication);
        this.userFollowApplication = userFollowApplication;
    }
    
    @Override
    public boolean supports(String forumName) {
        return "follow".equals(forumName);
    }
    
    @Override
    public Page<ThreadPO> query(ThreadQuery threadQuery) {
        // 获取当前用户关注的用户ID列表
        Long currentUserId = HeaderUtils.getUserId();
        if (currentUserId == null || currentUserId == 0) {
            // 未登录用户返回空结果
            return new Page<>(threadQuery.getPageNo(), threadQuery.getPageSize());
        }
        
        // 获取用户关注的所有作者
        List<Long> followingUserIds = userFollowApplication.getFollowingList(currentUserId, 1, 1000)
                .getRecords()
                .stream()
                .map(UserFollowVO::getUserId)
                .collect(Collectors.toList());
        
        if (followingUserIds.isEmpty()) {
            // 没有关注任何人，返回空结果
            return new Page<>(threadQuery.getPageNo(), threadQuery.getPageSize());
        }
        
        // 查询这些作者发布的帖子
        return threadService.queryThreadsByUserIds(followingUserIds, threadQuery.getPageNo(), threadQuery.getPageSize());
    }
} 