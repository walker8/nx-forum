package com.leyuz.bbs.follow.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.bbs.follow.UserFollowExtPO;
import com.leyuz.bbs.follow.UserFollowPO;
import com.leyuz.bbs.user.mybatis.ForumUserPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户关注表 服务实现类
 * </p>
 *
 * @author walker
 * @since 2025-03-01
 */
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollowPO> implements IUserFollowService {

    private final ForumUserPropertyService forumUserPropertyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long userId, Long followUserId, String remark) {
        // 不能关注自己
        if (userId.equals(followUserId)) {
            return false;
        }

        // 检查是否已关注
        if (isFollowing(userId, followUserId)) {
            return false;
        }

        // 创建关注记录
        UserFollowPO followPO = UserFollowPO.builder()
                .createBy(userId)
                .followUserId(followUserId)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();

        boolean result = save(followPO);
        
        // 增加被关注用户的粉丝数
        if (result) {
            forumUserPropertyService.incrementFans(followUserId);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollowPO> queryWrapper = new LambdaQueryWrapper<UserFollowPO>()
                .eq(UserFollowPO::getCreateBy, userId)
                .eq(UserFollowPO::getFollowUserId, followUserId);

        boolean result = remove(queryWrapper);
        
        // 减少被关注用户的粉丝数
        if (result) {
            forumUserPropertyService.decrementFans(followUserId);
        }
        
        return result;
    }

    @Override
    public boolean isFollowing(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollowPO> queryWrapper = new LambdaQueryWrapper<UserFollowPO>()
                .eq(UserFollowPO::getCreateBy, userId)
                .eq(UserFollowPO::getFollowUserId, followUserId);

        return count(queryWrapper) > 0;
    }

    @Override
    public List<UserFollowExtPO> getFollowingList(Page<UserFollowExtPO> page, Long userId) {
        return baseMapper.selectFollowingList(page, userId);
    }

    @Override
    public List<UserFollowExtPO> getFollowerList(Page<UserFollowExtPO> page, Long userId) {
        return baseMapper.selectFollowerList(page, userId);
    }
} 