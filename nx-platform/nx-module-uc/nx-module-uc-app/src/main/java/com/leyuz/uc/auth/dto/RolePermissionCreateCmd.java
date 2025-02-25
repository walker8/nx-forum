package com.leyuz.uc.auth.dto;

import lombok.Data;

@Data
public class RolePermissionCreateCmd {

    private String roleKey;

    private String perms;
} 