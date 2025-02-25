package com.leyuz.uc.auth.role.dto;

import com.alibaba.cola.dto.Query;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

@Data
public class UserRolePageQuery extends Query {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "角色key", example = "")
    private String roleKey = "";

    @Schema(description = "角色范围", example = "")
    private String roleScope = "";

    @Schema(description = "用户名", example = "")
    private String userName = "";

    @Schema(description = "用户id", example = "0")
    private Long userId = 0L;
}