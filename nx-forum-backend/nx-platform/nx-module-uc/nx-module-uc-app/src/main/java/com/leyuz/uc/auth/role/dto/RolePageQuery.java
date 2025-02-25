package com.leyuz.uc.auth.role.dto;

import com.alibaba.cola.dto.Query;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;

@Data
@Builder
public class RolePageQuery extends Query {
    @Serial
    private static final long serialVersionUID = 4141751874793888130L;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String roleName;
    private Integer roleStatus;
} 