package com.leyuz.uc.config.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "认证配置信息")
public class AuthConfigVO {

    @Schema(description = "登录配置")
    private LoginConfigVO loginConfig;

    @Schema(description = "注册配置")
    private RegisterConfigVO registerConfig;
} 