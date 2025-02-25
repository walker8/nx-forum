package com.leyuz.uc.auth.permission.dto;

import com.alibaba.cola.dto.Command;
import lombok.Data;

import java.io.Serial;

@Data
public class PermissionCreateCmd extends Command {
    @Serial
    private static final long serialVersionUID = -3947882870355842668L;
    private Long parentId;
    private String permName;
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