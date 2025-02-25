package com.leyuz.uc.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录配置")
public class LoginConfigVO {

    @Schema(description = "是否开启手机验证码登录")
    private boolean enablePhoneCodeLogin = true;

    @Schema(description = "是否开启邮箱验证码登录")
    private boolean enableEmailCodeLogin = true;

    @Schema(description = "是否开启账密登录")
    private boolean enablePasswordLogin = true;

    @Schema(description = "是否开启登录验证码")
    private boolean enableLoginCaptcha = true;

    @Schema(description = "是否开启手机找回密码")
    private boolean enablePhoneResetPassword = true;

    @Schema(description = "是否开启邮箱找回密码")
    private boolean enableEmailResetPassword = true;
} 