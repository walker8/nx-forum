package com.leyuz.bbs.interaction.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户关注表扩展对象，用于JOIN查询
 * </p>
 *
 * @author walker
 * @since 2025-03-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserFollowExtPO extends UserFollowPO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;
} 