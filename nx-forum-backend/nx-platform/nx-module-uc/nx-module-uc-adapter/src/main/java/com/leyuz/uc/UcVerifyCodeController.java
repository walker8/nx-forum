package com.leyuz.uc;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import com.leyuz.uc.user.verify.VerifyCodeApplication;
import com.leyuz.uc.user.verify.dto.ChangeToNewEmailCmd;
import com.leyuz.uc.user.verify.dto.ResetPasswordDTO;
import com.leyuz.uc.user.dto.UserResp;
import com.leyuz.uc.user.auth.dto.VerifyCodeLoginDTO;
import com.leyuz.uc.user.verify.dto.VerifyCurrentEmailCmd;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

/**
 * <p>
 * 验证码中心 - 统一管理所有验证码相关接口
 * </p>
 *
 * @author walker
 * @since 2025-10-04
 */
@Tag(name = "验证码中心")
@RestController
@RequestMapping("/v1/uc/verify-code")
public class UcVerifyCodeController {

    @Autowired
    private VerifyCodeApplication verifyCodeApplication;

    /**
     * 发送邮箱验证码
     * 支持多种场景：注册、登录、重置密码、换绑邮箱
     *
     * @param email 邮箱地址 (当type=change_email且target=current时可不传，后端自动获取当前用户邮箱)
     * @param type 验证码类型：register(注册)、login(登录)、reset_password(重置密码)、change_email(换绑邮箱)
     * @param target 当type=change_email时有效：current(发送到当前用户邮箱)、new(发送到新邮箱)
     */
    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email")
    @PermitAll
    @RateLimiter(rules = {
            @RateLimitRule(key = "#email+'_'+#type+'_'+#target", time = 50, count = 1, timeUnit = TimeUnit.SECONDS, message = "当前请求过于频繁，请60秒后再试"),
            @RateLimitRule(key = "#email", time = 1, count = 10, timeUnit = TimeUnit.HOURS, message = "当前IP请求过于频繁，请1小时后再试"),
            @RateLimitRule(key = "email", time = 1, count = 50, timeUnit = TimeUnit.HOURS, limitType = LimitType.IP, message = "当前IP请求过于频繁，请1小时后再试")
    })
    public SingleResponse sendEmailVerifyCode(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "register") String type,
            @RequestParam(required = false) String target) {
        verifyCodeApplication.sendEmailVerifyCode(email, type, target);
        return SingleResponse.buildSuccess();
    }

    /**
     * 发送手机验证码
     * 支持多种场景：注册、登录、重置密码
     */
    @Operation(summary = "发送手机验证码")
    @PostMapping("/phone")
    @PermitAll
    @RateLimiter(rules = {
            @RateLimitRule(key = "#phone+'_'+#type", time = 50, count = 1, timeUnit = TimeUnit.SECONDS, message = "当前请求过于频繁，请60秒后再试"),
            @RateLimitRule(key = "#phone", time = 1, count = 8, timeUnit = TimeUnit.HOURS, message = "当前IP请求过于频繁，请1小时后再试"),
            @RateLimitRule(key = "phone", time = 1, count = 30, timeUnit = TimeUnit.HOURS, limitType = LimitType.IP, message = "当前IP请求过于频繁，请1小时后再试")
    })
    public SingleResponse sendPhoneVerifyCode(
            @RequestParam String phone,
            @RequestParam(defaultValue = "register") String type) {
        verifyCodeApplication.sendPhoneVerifyCode(phone, type);
        return SingleResponse.buildSuccess();
    }

    /**
     * 验证码登录
     * 支持邮箱和手机验证码登录
     */
    @Operation(summary = "验证码登录")
    @PostMapping("/login")
    @PermitAll
    public SingleResponse<UserResp> verifyCodeLogin(@RequestBody VerifyCodeLoginDTO loginDTO) {
        return SingleResponse.of(verifyCodeApplication.verifyCodeLogin(loginDTO));
    }

    /**
     * 验证码重置密码
     * 支持邮箱和手机验证码重置密码
     */
    @Operation(summary = "验证码重置密码")
    @PostMapping("/reset-password")
    @PermitAll
    public SingleResponse resetPasswordByVerifyCode(@RequestBody ResetPasswordDTO resetDTO) {
        verifyCodeApplication.resetPasswordByVerifyCode(
                resetDTO.getAccount(),
                resetDTO.getCode(),
                resetDTO.getNewPassword(),
                resetDTO.getType()
        );
        return SingleResponse.buildSuccess();
    }


    /**
     * 验证当前邮箱
     * 验证当前用户邮箱的验证码
     */
    @Operation(summary = "验证当前邮箱")
    @PostMapping("/current-user/email/verify")
    public SingleResponse verifyCurrentEmail(@Valid @RequestBody VerifyCurrentEmailCmd cmd) {
        verifyCodeApplication.verifyCurrentEmail(cmd);
        return SingleResponse.of("当前邮箱验证成功");
    }

    /**
     * 设置新邮箱
     * 完成邮箱换绑操作
     */
    @Operation(summary = "设置新邮箱")
    @PostMapping("/current-user/email/change")
    public SingleResponse changeToNewEmail(@Valid @RequestBody ChangeToNewEmailCmd cmd) {
        verifyCodeApplication.changeToNewEmail(cmd);
        return SingleResponse.of("邮箱换绑成功");
    }
}
