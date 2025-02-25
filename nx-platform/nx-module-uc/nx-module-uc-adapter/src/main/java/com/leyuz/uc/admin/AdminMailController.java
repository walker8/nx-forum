package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.module.mail.application.MailApplication;
import com.leyuz.module.mail.application.MailApplication.MailTemplateType;
import com.leyuz.module.mail.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "邮件服务管理")
@RestController
@RequestMapping("/v1/uc/admin/mail")
@RequiredArgsConstructor
public class AdminMailController {

    private final MailApplication mailApplication;

    @Operation(summary = "更新SMTP配置")
    @PostMapping("/smtp/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateSmtpConfig(@Valid @RequestBody MailConfigDTO cmd) {
        mailApplication.updateSmtpConfig(cmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取SMTP配置")
    @GetMapping("/smtp/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<MailConfigDTO> getSmtpConfig() {
        return SingleResponse.of(mailApplication.getSmtpConfig());
    }

    @Operation(summary = "更新邮件域名黑白名单配置")
    @PostMapping("/domain/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateDomainConfig(@Valid @RequestBody EmailListConfigDTO config) {
        mailApplication.updateEmailListConfig(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取邮件域名黑白名单配置")
    @GetMapping("/domain/config")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<EmailListConfigDTO> getDomainConfig() {
        return SingleResponse.of(mailApplication.getEmailListConfig());
    }

    @Operation(summary = "获取允许的邮件域名列表")
    @GetMapping("/domain/allowed")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<Set<String>> getAllowedDomains() {
        return SingleResponse.of(mailApplication.getEmailListConfig().getAllowedDomains());
    }

    @Operation(summary = "添加允许的邮件域名")
    @PostMapping("/domain/allowed/{domain}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse addAllowedDomain(@PathVariable String domain) {
        mailApplication.addAllowedDomain(domain);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "移除允许的邮件域名")
    @DeleteMapping("/domain/allowed/{domain}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse removeAllowedDomain(@PathVariable String domain) {
        mailApplication.removeAllowedDomain(domain);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "添加禁止的邮件域名")
    @PostMapping("/domain/blocked/{domain}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse addBlockedDomain(@PathVariable String domain) {
        mailApplication.addBlockedDomain(domain);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "移除禁止的邮件域名")
    @DeleteMapping("/domain/blocked/{domain}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse removeBlockedDomain(@PathVariable String domain) {
        mailApplication.removeBlockedDomain(domain);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "更新邮件模板")
    @PostMapping("/template/{type}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateMailTemplate(
            @PathVariable MailTemplateType type,
            @Valid @RequestBody MailTemplateDTO template) {
        mailApplication.updateMailTemplate(type, template);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取邮件模板")
    @GetMapping("/template/{type}")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<MailTemplateDTO> getMailTemplate(@PathVariable MailTemplateType type) {
        return SingleResponse.of(mailApplication.getMailTemplate(type));
    }

    @Operation(summary = "测试模板邮件发送 - 验证码")
    @PostMapping("/template/test/verify-code")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse testVerifyCodeMail(@Valid @RequestBody TemplateTestCmd cmd) {
        // 设置默认值
        if (!cmd.getVariables().containsKey("siteName")) {
            cmd.getVariables().put("siteName", "测试站点");
        }
        if (!cmd.getVariables().containsKey("userName")) {
            cmd.getVariables().put("userName", "测试用户");
        }
        if (!cmd.getVariables().containsKey("expireMinutes")) {
            cmd.getVariables().put("expireMinutes", "5");
        }

        mailApplication.sendTemplateEmail(MailTemplateType.VERIFY_CODE, cmd.getTo(), cmd.getVariables());
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "测试模板邮件发送 - 重置密码")
    @PostMapping("/template/test/reset-password")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse testResetPasswordMail(@Valid @RequestBody TemplateTestCmd cmd) {
        // 设置默认值
        if (!cmd.getVariables().containsKey("siteName")) {
            cmd.getVariables().put("siteName", "测试站点");
        }
        if (!cmd.getVariables().containsKey("userName")) {
            cmd.getVariables().put("userName", "测试用户");
        }
        if (!cmd.getVariables().containsKey("expireMinutes")) {
            cmd.getVariables().put("expireMinutes", "30");
        }

        mailApplication.sendTemplateEmail(MailTemplateType.RESET_PASSWORD, cmd.getTo(), cmd.getVariables());
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "测试模板邮件发送 - 注册成功")
    @PostMapping("/template/test/register-success")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse testRegisterSuccessMail(@Valid @RequestBody TemplateTestCmd cmd) {
        // 设置默认值
        if (!cmd.getVariables().containsKey("siteName")) {
            cmd.getVariables().put("siteName", "测试站点");
        }
        if (!cmd.getVariables().containsKey("supportEmail")) {
            cmd.getVariables().put("supportEmail", "support@example.com");
        }

        mailApplication.sendTemplateEmail(MailTemplateType.REGISTER_SUCCESS, cmd.getTo(), cmd.getVariables());
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "测试邮件发送")
    @PostMapping("/test")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse testMail(@Valid @RequestBody MailTestCmd cmd) {
        MailMessage message = new MailMessage();
        message.setTo(List.of(cmd.getTo()));
        message.setFrom(cmd.getFrom());
        message.setSubject(cmd.getSubject());
        message.setContent(cmd.getContent());
        message.setHtml(cmd.isHtml());

        mailApplication.sendMail(message);
        return SingleResponse.buildSuccess();
    }
}