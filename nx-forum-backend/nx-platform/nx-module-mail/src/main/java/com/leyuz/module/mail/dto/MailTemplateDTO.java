package com.leyuz.module.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "邮件模板")
public class MailTemplateDTO {

    @Schema(description = "模板主题")
    @NotBlank(message = "模板主题不能为空")
    private String subject;

    @Schema(description = "模板内容")
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "是否HTML内容")
    private boolean html = true;

    @Schema(description = "模板说明")
    private String description;
} 