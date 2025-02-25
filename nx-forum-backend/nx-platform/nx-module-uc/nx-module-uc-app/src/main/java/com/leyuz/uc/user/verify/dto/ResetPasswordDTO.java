package com.leyuz.uc.user.verify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码重置密码请求")
public class ResetPasswordDTO {
    @Schema(description = "账号(手机号/邮箱)")
    private String account;
    
    @Schema(description = "验证码")
    private String code;
    
    @Schema(description = "新密码")
    private String newPassword;
    
    @Schema(description = "账号类型(phone/email)")
    private String type;
} 