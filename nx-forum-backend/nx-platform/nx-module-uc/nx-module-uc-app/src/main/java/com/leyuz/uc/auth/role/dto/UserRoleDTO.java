package com.leyuz.uc.auth.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleDTO {
    @Schema(description = "用户角色ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "角色Key", example = "ADMIN")
    private String roleKey;

    @Schema(description = "角色范围", example = "GLOBAL")
    private String roleScope;

    @Schema(description = "创建者ID", example = "1002")
    private Long createBy;

    @Schema(description = "创建时间", example = "2023-01-01T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "过期时间", example = "2023-12-31T23:59:59")
    private LocalDateTime expireTime;
}
