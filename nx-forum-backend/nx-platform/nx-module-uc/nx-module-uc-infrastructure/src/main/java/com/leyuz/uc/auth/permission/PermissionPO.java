package com.leyuz.uc.auth.permission;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("uc_permissions")
public class PermissionPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -3639893415420169188L;
    @TableId(value = "perm_id", type = IdType.AUTO)
    private Long permId;

    /**
     * 父权限ID
     */
    private Long parentId;

    private String permName;

    private Integer permStatus;

    private String perms;

    private String remark;

    /**
     * 权限类型（1目录 2菜单 3按钮）
     */
    private Integer permType;
    /**
     * 显示顺序
     */
    private Integer permOrder;
} 