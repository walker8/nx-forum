package com.leyuz.uc.auth.role;

import com.leyuz.uc.auth.role.dataobject.RoleStatusV;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleE {
    private final Integer roleId;
    private final String roleName;
    private final Integer priority;
    private final String roleKey;
    private final String remark;
    private final RoleStatusV roleStatus;
} 