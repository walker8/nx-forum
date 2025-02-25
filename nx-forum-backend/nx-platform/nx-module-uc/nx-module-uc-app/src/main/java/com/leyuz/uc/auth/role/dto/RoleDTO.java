package com.leyuz.uc.auth.role.dto;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO extends DTO {
    @Serial
    private static final long serialVersionUID = 2882017780788303493L;
    private Integer roleId;
    private String roleName;
    private String roleKey;
    private String remark;
    private Integer roleStatus;
    private String roleStatusDesc;
    private List<String> perms;
    private Integer priority;
} 