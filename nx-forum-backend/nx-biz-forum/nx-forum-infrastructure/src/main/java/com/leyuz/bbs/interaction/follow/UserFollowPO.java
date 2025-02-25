package com.leyuz.bbs.interaction.follow;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户关注表
 * </p>
 *
 * @author walker
 * @since 2025-03-01
 */
@TableName("bbs_user_follows")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowPO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关注者用户ID
     */
    private Long createBy;

    /**
     * 被关注的用户ID
     */
    private Long followUserId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关注时间
     */
    private LocalDateTime createTime;
} 