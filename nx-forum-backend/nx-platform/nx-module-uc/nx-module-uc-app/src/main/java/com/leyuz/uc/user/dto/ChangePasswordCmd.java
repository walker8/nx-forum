package com.leyuz.uc.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改密码请求（旧密码方式）")
public class ChangePasswordCmd {
    @Schema(description = "当前密码")
    private String oldPassword;

    @Schema(description = "新密码")
    private String newPassword;
}
