package com.leyuz.uc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.uc.config.AuthConfigVO;
import com.leyuz.uc.config.LoginConfigApplication;
import com.leyuz.uc.config.LoginConfigVO;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.config.RegisterConfigVO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserLoginDTO;
import com.leyuz.uc.user.dto.UserResp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;

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

}
