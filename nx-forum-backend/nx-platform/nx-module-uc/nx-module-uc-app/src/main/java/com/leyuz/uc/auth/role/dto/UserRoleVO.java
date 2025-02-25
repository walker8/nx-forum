package com.leyuz.uc.auth.role.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class UserRoleVO {

    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;
    @Schema(description = "用户名", example = "admin")
    private String userName;

    @Schema(description = "角色Key", example = "ADMIN")
    private String roleKey;

    @Schema(description = "角色名称", example = "ADMIN")
    private String roleName;

    @Schema(description = "角色范围", example = "GLOBAL")
    private String roleScope;

    @Schema(description = "创建者用户名", example = "admin")
    private String createUserName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间", example = "")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireTime;
}
