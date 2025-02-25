package com.leyuz.uc.auth.role.dto;

import com.alibaba.cola.dto.Command;
import lombok.Data;

import java.io.Serial;
import java.util.List;

@Data
public class RoleUpdateCmd extends Command {
    @Serial
    private static final long serialVersionUID = -310342644429925896L;
    private Integer roleId;
    private String roleName;
    private String remark;
    private Integer roleStatus;
    private List<String> perms;
    private Integer priority;
}