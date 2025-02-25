package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("uc_user_roles")
public class UserRolePO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8153518450371007078L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String roleKey;

    private String roleScope;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
} 