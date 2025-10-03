package com.leyuz.uc;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.ChangeToNewEmailCmd;
import com.leyuz.uc.user.dto.UserAccountVO;
import com.leyuz.uc.user.dto.UserCmd;
import com.leyuz.uc.user.dto.UserVO;
import com.leyuz.uc.user.dto.VerifyCurrentEmailCmd;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

/**
 * <p>
 * 用户中心
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
@Tag(name = "用户中心")
@RestController
@RequestMapping("/v1/uc/users")
public class UcUserController {

    @Autowired
    private UserApplication userApplication;
    @Autowired
    private RegisterConfigApplication registerConfigApplication;

    @Operation(summary = "用户注册")
    @PostMapping
    @PermitAll
    public SingleResponse saveUser(@RequestBody UserCmd userCmd) {
        userApplication.saveUserWithVerifyCode(userCmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取当前用户登录信息")
    @GetMapping("/current")
    @PermitAll
    public SingleResponse getCurrentUser() {
        UserVO userVO = userApplication.getCurrentUser();
        return SingleResponse.of(userVO);
    }

    @Operation(summary = "获取用户基本信息")
    @GetMapping("/{userId}/info")
    @PermitAll
    public SingleResponse getUserInfo(@PathVariable Long userId) {
        UserVO userVO = userApplication.getUserInfo(userId);
        return SingleResponse.of(userVO);
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/current")
    public SingleResponse updateCurrentUser(@RequestBody UserVO userVO) {
        userApplication.updateCurrentUser(userVO);
        return SingleResponse.of("用户信息更新成功!");
    }

    @Operation(summary = "获取当前用户账号信息")
    @GetMapping("/current/account")
    public SingleResponse getCurrentUserAccount() {
        UserAccountVO userVO = userApplication.getUserAccount();
        return SingleResponse.of(userVO);
    }

    @Operation(summary = "检查用户名是否被禁止")
    @GetMapping("/username/check")
    @PermitAll
    public SingleResponse<Boolean> checkUsername(@RequestParam String username) {
        userApplication.checkUsername(username);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "根据名称模糊查询已注册的用户")
    @GetMapping("/username/{name}")
    public SingleResponse<List<UserVO>> queryUsername(@PathVariable String name) {
        List<UserVO> userList = userApplication.queryUsername(name);
        return SingleResponse.of(userList);
    }

    @Operation(summary = "发送验证码到当前用户邮箱")
    @PostMapping("/current/email/send-verify-code")
    public SingleResponse sendCurrentUserEmailVerifyCode() {
        userApplication.sendCurrentUserEmailVerifyCode();
        return SingleResponse.of("验证码已发送到您的邮箱，请查收");
    }

    @Operation(summary = "发送验证码到新邮箱")
    @PostMapping("/current/email/send-new-verify-code")
    public SingleResponse sendNewEmailVerifyCode(@RequestParam String newEmail) {
        userApplication.sendNewEmailVerifyCode(newEmail);
        return SingleResponse.of("验证码已发送到新邮箱，请查收");
    }

    @Operation(summary = "验证当前邮箱")
    @PostMapping("/current/email/verify-current")
    public SingleResponse verifyCurrentEmail(@Valid @RequestBody VerifyCurrentEmailCmd cmd) {
        userApplication.verifyCurrentEmail(cmd);
        return SingleResponse.of("当前邮箱验证成功");
    }

    @Operation(summary = "设置新邮箱")
    @PostMapping("/current/email/change-to-new")
    public SingleResponse changeToNewEmail(@Valid @RequestBody ChangeToNewEmailCmd cmd) {
        userApplication.changeToNewEmail(cmd);
        return SingleResponse.of("邮箱换绑成功");
    }
}
