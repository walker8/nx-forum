package com.leyuz.uc.user.auth.dto;

import com.alibaba.cola.dto.DTO;
import lombok.Data;

import java.io.Serial;

@Data
public class UserLoginDTO extends DTO {
    @Serial
    private static final long serialVersionUID = -2805313706775062608L;

    /**
     * 手机号码/邮箱/用户名
     */
    private String userName;
    /**
     * 登录密码
     */
    private String password;
    /**
     * 验证码
     */
    private String captchaCode;
}
