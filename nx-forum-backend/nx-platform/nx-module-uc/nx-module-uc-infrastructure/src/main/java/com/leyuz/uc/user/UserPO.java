package com.leyuz.uc.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
@TableName("uc_users")
@Data
public class UserPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -14265746510701003L;
    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 最后活跃IP
     */
    private String lastActiveIp;

    /**
     * 个人介绍
     */
    private String intro;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveDate;

    /**
     * 帐号状态（0正常 1停用 2注销）
     */
    private Byte accountStatus;
}
