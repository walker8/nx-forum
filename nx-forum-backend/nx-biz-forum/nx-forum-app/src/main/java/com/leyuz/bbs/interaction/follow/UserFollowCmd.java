package com.leyuz.bbs.interaction.follow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户关注命令
 *
 * @author walker
 * @since 2025-03-01
 */
@Data
public class UserFollowCmd {

    /**
     * 被关注的用户ID
     */
    @NotNull
    private Long followUserId;

    /**
     * 备注
     */
    private String remark;
} 