package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.Command;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

@Schema(description = "用户表单对象")
@Data
public class UserCmd extends Command {

    @Serial
    private static final long serialVersionUID = -1978630342991538220L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "验证码")
    private String verifyCode;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "头像")
    private String avatar;

}
