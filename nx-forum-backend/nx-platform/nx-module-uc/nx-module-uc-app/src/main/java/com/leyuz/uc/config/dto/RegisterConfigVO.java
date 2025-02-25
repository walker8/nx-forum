package com.leyuz.uc.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册配置")
public class RegisterConfigVO {

    @Schema(description = "是否开启注册功能")
    private boolean enableRegister = true;

    @Schema(description = "是否开启短信注册")
    private boolean enableSmsRegister = true;

    @Schema(description = "是否开启邮箱注册")
    private boolean enableEmailRegister = true;

    @Schema(description = "是否开启注册验证码")
    private boolean enableRegisterCaptcha = true;

    @Schema(description = "密码最小长度")
    private int passwordMinLength = 6;

    @Schema(description = "密码最大长度")
    private int passwordMaxLength = 20;

    @Schema(description = "密码强度要求（0:无要求, 1:必须包含数字和字母, 2:必须包含数字、字母和特殊字符）")
    private int passwordStrength = 1;
} 