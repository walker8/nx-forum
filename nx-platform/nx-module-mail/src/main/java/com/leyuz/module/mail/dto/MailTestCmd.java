package com.leyuz.module.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "邮件发送测试命令")
public class MailTestCmd {

    @Schema(description = "收件人邮箱")
    @NotBlank(message = "收件人邮箱不能为空")
    @Email(message = "收件人邮箱格式不正确")
    private String to;

    @Schema(description = "自定义发件人")
    @Email(message = "发件人邮箱格式不正确")
    private String from;

    @Schema(description = "邮件主题")
    @NotBlank(message = "邮件主题不能为空")
    private String subject;

    @Schema(description = "邮件内容")
    @NotBlank(message = "邮件内容不能为空")
    private String content;

    @Schema(description = "是否HTML内容")
    private boolean html = false;
} 