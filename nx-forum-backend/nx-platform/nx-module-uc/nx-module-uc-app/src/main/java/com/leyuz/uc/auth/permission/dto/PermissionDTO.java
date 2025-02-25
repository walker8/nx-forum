package com.leyuz.uc.auth.permission.dto;

import com.alibaba.cola.dto.DTO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

@Data
public class PermissionDTO extends DTO {
    @Serial
    private static final long serialVersionUID = -4013486895603223208L;
    private List<PermissionDTO> children;
    private Long permId;
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