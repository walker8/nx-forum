package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.security.SecretCrypto;
import com.leyuz.module.sms.application.SmsApplication;
import com.leyuz.module.sms.dto.SmsConfigDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "短信服务管理")
@RestController
@RequestMapping("/v1/uc/admin/sms")
@RequiredArgsConstructor
public class AdminSmsController {

    private final SmsApplication smsApplication;

    @Operation(summary = "更新短信配置")
    @PostMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateSmsConfig(@Valid @RequestBody SmsConfigDTO cmd) {
        smsApplication.updateSmsConfig(cmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取短信配置")
    @GetMapping("/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<SmsConfigDTO> getSmsConfig() {
        SmsConfigDTO config = smsApplication.getSmsConfig();
        // 管理端回显脱敏，明文仅在服务端发送短信时使用
        if (config != null) {
            config.setAccessKeySecret(SecretCrypto.mask(config.getAccessKeySecret()));
        }
        return SingleResponse.of(config);
    }

    @Operation(summary = "测试短信发送")
    @GetMapping("/test")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse testSms(@RequestParam String phoneNumber) {
        smsApplication.sendVerifyCode(phoneNumber);
        return SingleResponse.buildSuccess();
    }
}