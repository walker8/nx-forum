package com.leyuz.uc.auth.role.dto;

import com.alibaba.cola.dto.Command;
import lombok.Data;

import java.io.Serial;
import java.util.List;

@Data
public class RoleCreateCmd extends Command {
    @Serial
    private static final long serialVersionUID = -3016497166537075732L;
    private String roleName;
    private String roleKey;
    private String remark;
    private List<String> perms;
    private Integer priority;
}