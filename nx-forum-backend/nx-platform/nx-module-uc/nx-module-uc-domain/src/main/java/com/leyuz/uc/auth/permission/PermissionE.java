package com.leyuz.uc.auth.permission;

import com.leyuz.uc.auth.permission.dataobject.PermissionStatusV;
import com.leyuz.uc.auth.permission.dataobject.PermissionTypeV;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionE {
    private Long permId;
    private Long parentId;
    private String permName;
    @Builder.Default
    private PermissionStatusV permStatus = PermissionStatusV.ENABLED;
    private String perms;
    private String remark;
    /**
     * 权限类型（1目录 2菜单 3按钮）
     */
    private PermissionTypeV permType;
    /**
     * 显示顺序
     */
    private Integer permOrder;

    // 领域行为
    public void validate() {
        if (StringUtils.isBlank(permName)) {
            throw new IllegalArgumentException("权限名称不能为空");
        }
        if (permType == null) {
            throw new IllegalArgumentException("权限类型不能为空");
        }
        if (!permType.equals(PermissionTypeV.DIRECTORY) && StringUtils.isBlank(perms)) {
            // 非目录类型的权限必须有权限标识
            throw new IllegalArgumentException("权限标识不能为空");
        }
        if (permOrder == null) {
            permOrder = 0;
        }
        if (permType.equals(PermissionTypeV.DIRECTORY)) {
            perms = "";
        } else {
            validatePermsFormat();
        }
    }

    public void validatePermsFormat() {
        if (!perms.matches("^[a-z][a-z:_-]*$") && !perms.equals("*")) {
            throw new IllegalArgumentException("权限标识只能包含小写字母、冒号、连字符和下划线，且必须以字母开头");
        }
    }

    public PermissionE disable() {
        permStatus = PermissionStatusV.DISABLED;
        return this;
    }

    public PermissionE enable() {
        permStatus = PermissionStatusV.ENABLED;
        return this;
    }

    public boolean isEnabled() {
        return PermissionStatusV.ENABLED.equals(permStatus);
    }
} 