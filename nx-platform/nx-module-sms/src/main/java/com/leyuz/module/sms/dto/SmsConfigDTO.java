package com.leyuz.module.sms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "短信配置")
public class SmsConfigDTO {

    @Schema(description = "短信服务提供商", example = "ALIYUN")
    private String provider;

    @Schema(description = "访问密钥ID", example = "LTAI4XXXXX")
    private String accessKeyId;

    @Schema(description = "访问密钥密码", example = "XXXXX")
    private String accessKeySecret;

    @Schema(description = "短信签名", example = "XXXXX")
    private String signName;

    @Schema(description = "验证码模板ID", example = "SMS_123456789")
    private String verifyCodeTemplateId;

    @Schema(description = "手机号黑名单")
    private Set<String> blacklist;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;
} 