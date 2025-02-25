package com.leyuz.uc.auth.role;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("uc_roles")
public class RolePO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4700835498497942776L;
    @TableId(value = "role_id", type = IdType.AUTO)
    private Integer roleId;

    private String roleName;

    private String roleKey;

    /**
     * 优先级(数字越大优先级越高)
     */
    private Integer priority;

    private String remark;

    /**
     * 角色状态0-正常1-停用
     */
    private Integer roleStatus;
} 