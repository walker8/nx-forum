package com.leyuz.bbs.follow.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.bbs.follow.UserFollowExtPO;
import com.leyuz.bbs.follow.UserFollowPO;

import java.util.List;

/**
 * <p>
 * 用户关注表 服务接口
 * </p>
 *
 * @author walker
 * @since 2025-03-01
 */
public interface IUserFollowService extends IService<UserFollowPO> {

    /**
     * 关注用户
     *
     * @param userId       关注者用户ID
     * @param followUserId 被关注的用户ID
     * @param remark       备注
     * @return 是否成功
     */
    boolean follow(Long userId, Long followUserId, String remark);

    /**
     * 取消关注
     *
     * @param userId       关注者用户ID
     * @param followUserId 被关注的用户ID
     * @return 是否成功
     */
    boolean unfollow(Long userId, Long followUserId);

    /**
     * 查询是否已关注
     *
     * @param userId       关注者用户ID
     * @param followUserId 被关注的用户ID
     * @return 是否已关注
     */
    boolean isFollowing(Long userId, Long followUserId);

    /**
     * 查询用户关注列表
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 关注列表
     */
    List<UserFollowExtPO> getFollowingList(Page<UserFollowExtPO> page, Long userId);

    /**
     * 查询用户粉丝列表
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 粉丝列表
     */
    List<UserFollowExtPO> getFollowerList(Page<UserFollowExtPO> page, Long userId);
} 