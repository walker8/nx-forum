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
@TableName("uc_role_permissions")
public class RolePermissionPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -3251944829925393206L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String roleKey;

    private Long permId;
} 