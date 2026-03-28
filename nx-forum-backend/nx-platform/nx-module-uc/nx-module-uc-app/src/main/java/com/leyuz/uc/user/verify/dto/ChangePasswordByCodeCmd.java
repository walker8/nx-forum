package com.leyuz.uc.user.verify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "验证码修改密码请求（已登录用户）")
public class ChangePasswordByCodeCmd {
    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
