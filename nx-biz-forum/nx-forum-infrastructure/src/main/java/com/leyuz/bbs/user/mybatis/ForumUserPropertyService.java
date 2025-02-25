package com.leyuz.bbs.user.mybatis;

import com.leyuz.bbs.user.ForumUserPropertyDO;

public interface ForumUserPropertyService {
    /**
     * 根据用户ID获取论坛用户属性
     *
     * @param userId 用户ID
     * @return 论坛用户属性
     */
    ForumUserPropertyDO getByUserId(Long userId);

    /**
     * 初始化用户论坛属性
     *
     * @param userId 用户ID
     */
    void initUserProperty(Long userId);

    /**
     * 增加发帖数
     *
     * @param userId 用户ID
     */
    void incrementThreads(Long userId);

    /**
     * 减少发帖数
     *
     * @param userId 用户ID
     */
    void decrementThreads(Long userId);

    /**
     * 增加评论数
     *
     * @param userId 用户ID
     */
    void incrementComments(Long userId);

    /**
     * 减少评论数
     *
     * @param userId 用户ID
     */
    void decrementComments(Long userId);

    /**
     * 增加粉丝数
     *
     * @param userId 用户ID
     */
    void incrementFans(Long userId);

    /**
     * 减少粉丝数
     *
     * @param userId 用户ID
     */
    void decrementFans(Long userId);

    /**
     * 增加积分
     *
     * @param userId 用户ID
     * @param amount 增加的积分数量
     */
    void addCredits(Long userId, int amount);

    /**
     * 增加金币
     *
     * @param userId 用户ID
     * @param amount 增加的金币数量
     */
    void addGolds(Long userId, int amount);
} 