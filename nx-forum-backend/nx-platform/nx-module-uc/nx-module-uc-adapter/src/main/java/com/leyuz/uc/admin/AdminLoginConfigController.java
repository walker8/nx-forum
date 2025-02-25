package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.uc.config.LoginConfigApplication;
import com.leyuz.uc.config.dto.LoginConfigDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录配置管理")
@RestController
@RequestMapping("/v1/uc/admin/login")
@RequiredArgsConstructor
public class AdminLoginConfigController {

    private final LoginConfigApplication loginConfigApplication;

    @Operation(summary = "更新登录配置")
    @PostMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateLoginConfig(@Valid @RequestBody LoginConfigDTO config) {
        loginConfigApplication.updateLoginConfig(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取登录配置")
    @GetMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<LoginConfigDTO> getLoginConfig() {
        return SingleResponse.of(loginConfigApplication.getLoginConfig());
    }
} 