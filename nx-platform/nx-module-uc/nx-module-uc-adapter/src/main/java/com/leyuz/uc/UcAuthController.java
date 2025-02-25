package com.leyuz.uc;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import com.leyuz.uc.config.*;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.ResetPasswordDTO;
import com.leyuz.uc.user.dto.UserLoginDTO;
import com.leyuz.uc.user.dto.UserResp;
import com.leyuz.uc.user.dto.VerifyCodeLoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Tag(name = "认证中心")
@RestController
@RequestMapping("/v1/uc/auth")
public class UcAuthController {
    @Autowired
    UserApplication userApplication;
    @Autowired
    RegisterConfigApplication registerConfigApplication;
    @Autowired
    LoginConfigApplication loginConfigApplication;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public SingleResponse<UserResp> login(@RequestBody UserLoginDTO userLoginDTO) {
        return SingleResponse.of(userApplication.login(userLoginDTO));
    }

    @Operation(summary = "登出系统")
    @PostMapping("/logout")
    public SingleResponse logout() {
        userApplication.logout();
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取认证配置")
    @GetMapping("/config")
    @PermitAll
    public SingleResponse<AuthConfigVO> getAuthConfig() {
        AuthConfigVO authConfigVO = new AuthConfigVO();

        // 获取登录配置
        LoginConfigVO loginConfigVO = loginConfigApplication.getLoginConfigVO();
        authConfigVO.setLoginConfig(loginConfigVO);

        // 获取注册配置
        RegisterConfigVO registerConfigVO = registerConfigApplication.getRegisterConfigVO();
        authConfigVO.setRegisterConfig(registerConfigVO);

        return SingleResponse.of(authConfigVO);
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email/verify-code")
    @PermitAll
    @RateLimiter(rules = {
            @RateLimitRule(key = "#email+'_'+#type", time = 60, count = 1, timeUnit = TimeUnit.SECONDS, message = "当前请求过于频繁，请60秒后再试"),
            @RateLimitRule(key = "#email", time = 1, count = 10, timeUnit = TimeUnit.HOURS, message = "当前IP请求过于频繁，请1小时后再试"),
            @RateLimitRule(key = "email", time = 1, count = 50, timeUnit = TimeUnit.HOURS, limitType = LimitType.IP, message = "当前IP请求过于频繁，请1小时后再试")
    })
    public SingleResponse sendEmailVerifyCode(
            @RequestParam String email,
            @RequestParam(defaultValue = "register") String type) {
        userApplication.sendEmailVerifyCode(email, type);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "发送手机验证码")
    @PostMapping("/phone/verify-code")
    @PermitAll
    @RateLimiter(rules = {
            @RateLimitRule(key = "#phone+'_'+#type", time = 60, count = 1, timeUnit = TimeUnit.SECONDS, message = "当前请求过于频繁，请60秒后再试"),
            @RateLimitRule(key = "#phone", time = 1, count = 8, timeUnit = TimeUnit.HOURS, message = "当前IP请求过于频繁，请1小时后再试"),
            @RateLimitRule(key = "phone", time = 1, count = 30, timeUnit = TimeUnit.HOURS, limitType = LimitType.IP, message = "当前IP请求过于频繁，请1小时后再试")
    })
    public SingleResponse sendPhoneVerifyCode(
            @RequestParam String phone,
            @RequestParam(defaultValue = "register") String type) {
        userApplication.sendPhoneVerifyCode(phone, type);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "验证码登录")
    @PostMapping("/verify-code/login")
    @PermitAll
    public SingleResponse<UserResp> verifyCodeLogin(@RequestBody VerifyCodeLoginDTO loginDTO) {
        return SingleResponse.of(userApplication.verifyCodeLogin(loginDTO));
    }

    @Operation(summary = "验证码重置密码")
    @PostMapping("/reset-password")
    @PermitAll
    public SingleResponse resetPasswordByVerifyCode(@RequestBody ResetPasswordDTO resetDTO) {
        userApplication.resetPasswordByVerifyCode(
                resetDTO.getAccount(),
                resetDTO.getCode(),
                resetDTO.getNewPassword(),
                resetDTO.getType()
        );
        return SingleResponse.buildSuccess();
    }
}
