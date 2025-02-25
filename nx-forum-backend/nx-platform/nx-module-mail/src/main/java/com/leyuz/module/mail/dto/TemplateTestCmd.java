package com.leyuz.module.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "模板邮件测试命令")
public class TemplateTestCmd {
    
    @Schema(description = "收件人邮箱")
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "收件人邮箱格式不正确")
    private String to;
    
    @Schema(description = "模板变量")
    private Map<String, String> variables;
} 