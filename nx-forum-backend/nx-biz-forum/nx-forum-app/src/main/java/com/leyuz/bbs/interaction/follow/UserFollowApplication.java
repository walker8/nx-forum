package com.leyuz.bbs.interaction.follow;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.interaction.follow.UserFollowMapper;
import com.leyuz.bbs.user.property.ForumUserPropertyMapper;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户关注应用服务
 *
 * @author walker
 * @since 2025-03-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowApplication {

    private final UserFollowMapper userFollowMapper;
    private final ForumUserPropertyMapper forumUserPropertyMapper;

    /**
     * 关注用户
     *
     * @param cmd 关注命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(UserFollowCmd cmd) {
        Long currentUserId = HeaderUtils.getUserId();
        if (currentUserId == null || currentUserId == 0) {
            throw new ValidationException("请先登录");
        }

        if (currentUserId.equals(cmd.getFollowUserId())) {
            throw new ValidationException("不能关注自己");
        }

        // 检查是否已关注
        if (userFollowMapper.isFollowing(currentUserId, cmd.getFollowUserId())) {
            return false;
        }

        // 创建关注记录
        UserFollowPO followPO = UserFollowPO.builder()
                .createBy(currentUserId)
                .followUserId(cmd.getFollowUserId())
                .remark(cmd.getRemark())
                .createTime(LocalDateTime.now())
                .build();

        boolean result = userFollowMapper.insert(followPO) > 0;
        
        // 增加被关注用户的粉丝数
        if (result) {
            forumUserPropertyMapper.incrementFans(cmd.getFollowUserId());
        }
        
        return result;
    }

    /**
     * 取消关注
     *
     * @param followUserId 被关注的用户ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long followUserId) {
        Long currentUserId = HeaderUtils.getUserId();
        if (currentUserId == null || currentUserId == 0) {
            throw new ValidationException("请先登录");
        }

        boolean result = userFollowMapper.unfollow(currentUserId, followUserId);
        
        // 减少被关注用户的粉丝数
        if (result) {
            forumUserPropertyMapper.decrementFans(followUserId);
        }
        
        return result;
    }

    /**
     * 查询是否已关注
     *
     * @param followUserId 被关注的用户ID
     * @return 是否已关注
     */
    public boolean isFollowing(Long followUserId) {
        Long currentUserId = HeaderUtils.getUserId();
        if (currentUserId == null || currentUserId == 0) {
            return false;
        }
        if (currentUserId.equals(followUserId)) {
            return false;
        }

        return userFollowMapper.isFollowing(currentUserId, followUserId);
    }

    /**
     * 查询用户关注列表
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 关注列表
     */
    public CustomPage<UserFollowVO> getFollowingList(int pageNo, int pageSize) {
        Long currentUserId = HeaderUtils.getUserId();
        if (currentUserId == null || currentUserId == 0) {
            throw new ValidationException("请先登录");
        }
        return getFollowingList(currentUserId, pageNo, pageSize);
    }

    /**
     * 查询用户关注列表
     *
     * @param userId   用户ID
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 关注列表
     */
    public CustomPage<UserFollowVO> getFollowingList(Long userId, int pageNo, int pageSize) {
        Page<UserFollowExtPO> page = new Page<>(pageNo, pageSize);
        List<UserFollowExtPO> followingList = userFollowMapper.selectFollowingList(page, userId);
        page.setRecords(followingList);

        return DataBaseUtils.createCustomPage(page, this::convertToVO);
    }

    /**
     * 查询用户粉丝列表
     *
     * @param userId   用户ID
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 粉丝列表
     */
    public CustomPage<UserFollowVO> getFollowerList(Long userId, int pageNo, int pageSize) {
        Page<UserFollowExtPO> page = new Page<>(pageNo, pageSize);
        List<UserFollowExtPO> followerList = userFollowMapper.selectFollowerList(page, userId);
        page.setRecords(followerList);

        CustomPage<UserFollowVO> customPage = DataBaseUtils.createCustomPage(page, this::convertToVO);
        customPage.getRecords().forEach(vo -> vo.setFollowed(isFollowing(vo.getUserId())));
        return customPage;
    }

    /**
     * 将PO转换为VO
     *
     * @param po PO对象
     * @return VO对象
     */
    private UserFollowVO convertToVO(UserFollowExtPO po) {
        UserFollowVO vo = new UserFollowVO();
        if (po != null) {
            vo.setUserId(po.getFollowUserId());
            vo.setUserName(po.getUserName());
            vo.setAvatar(po.getAvatar());
            vo.setRemark(po.getRemark());
            vo.setFollowTime(po.getCreateTime());
        }
        return vo;
    }
} 