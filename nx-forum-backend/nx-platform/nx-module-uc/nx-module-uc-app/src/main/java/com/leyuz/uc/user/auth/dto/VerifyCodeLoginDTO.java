package com.leyuz.uc.user.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码登录请求")
public class VerifyCodeLoginDTO {
    
    @Schema(description = "账号(手机号/邮箱)")
    private String account;
    
    @Schema(description = "验证码")
    private String code;
    
    @Schema(description = "账号类型(phone/email)")
    private String type;
} 