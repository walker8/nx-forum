package com.leyuz.bbs.interaction.follow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.interaction.follow.UserFollowExtPO;
import com.leyuz.bbs.interaction.follow.UserFollowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户关注表 Mapper 接口
 * </p>
 *
 * @author walker
 * @since 2025-03-01
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollowPO> {

    /**
     * 查询用户关注列表（带用户信息）
     */
    @Select("SELECT f.create_time, f.follow_user_id, f.create_by, u.user_name, u.avatar FROM bbs_user_follows f " +
            "LEFT JOIN uc_users u ON f.follow_user_id = u.user_id " +
            "WHERE f.create_by = #{userId} ORDER BY f.id DESC")
    List<UserFollowExtPO> selectFollowingList(Page<UserFollowExtPO> page, @Param("userId") Long userId);

    /**
     * 查询用户粉丝列表（带用户信息）
     */
    @Select("SELECT f.create_time, u.user_id as follow_user_id, u.user_name, u.avatar FROM bbs_user_follows f " +
            "LEFT JOIN uc_users u ON f.create_by = u.user_id " +
            "WHERE f.follow_user_id = #{userId} ORDER BY f.id DESC")
    List<UserFollowExtPO> selectFollowerList(Page<UserFollowExtPO> page, @Param("userId") Long userId);

    /**
     * 判断是否已关注
     *
     * @param userId       关注者ID
     * @param followUserId 被关注者ID
     * @return 是否已关注
     */
    default boolean isFollowing(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollowPO> queryWrapper = new LambdaQueryWrapper<UserFollowPO>()
                .eq(UserFollowPO::getCreateBy, userId)
                .eq(UserFollowPO::getFollowUserId, followUserId);
        return selectCount(queryWrapper) > 0;
    }

    /**
     * 取消关注
     *
     * @param userId       关注者ID
     * @param followUserId 被关注者ID
     * @return 是否成功
     */
    default boolean unfollow(Long userId, Long followUserId) {
        LambdaQueryWrapper<UserFollowPO> queryWrapper = new LambdaQueryWrapper<UserFollowPO>()
                .eq(UserFollowPO::getCreateBy, userId)
                .eq(UserFollowPO::getFollowUserId, followUserId);
        return delete(queryWrapper) > 0;
    }
} 