package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.uc.config.RegisterConfigApplication;
import com.leyuz.uc.config.dto.RegisterConfigDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "注册配置管理")
@RestController
@RequestMapping("/v1/uc/admin/register")
@RequiredArgsConstructor
public class AdminRegisterConfigController {

    private final RegisterConfigApplication registerConfigApplication;

    @Operation(summary = "更新注册配置")
    @PostMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateRegisterConfig(@Valid @RequestBody RegisterConfigDTO config) {
        registerConfigApplication.updateRegisterConfig(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取注册配置")
    @GetMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<RegisterConfigDTO> getRegisterConfig() {
        return SingleResponse.of(registerConfigApplication.getRegisterConfig());
    }
} 