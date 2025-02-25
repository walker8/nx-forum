package com.leyuz.module.mail.application;

import com.alibaba.fastjson2.JSON;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.module.config.app.ConfigApplication;
import com.leyuz.module.mail.config.MailConfiguration;
import com.leyuz.module.mail.dto.EmailListConfigDTO;
import com.leyuz.module.mail.dto.MailConfigDTO;
import com.leyuz.module.mail.dto.MailMessage;
import com.leyuz.module.mail.dto.MailTemplateDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailApplication implements ApplicationRunner {
    private final JavaMailSender mailSender;
    private final MailConfiguration mailConfiguration;
    private final ConfigApplication configApplication;

    private static final String MAIL_SMTP_CONFIG_KEY = "mail_smtp_config";
    private static final String MAIL_LIST_CONFIG_KEY = "mail_list_config";
    private static final String MAIL_TEMPLATE_KEY_PREFIX = "mail_template_";

    // 预定义的模板类型
    public enum MailTemplateType {
        VERIFY_CODE("验证码", "verify_code"),
        RESET_PASSWORD("重置密码", "reset_password"),
        REGISTER_SUCCESS("注册成功", "register_success");

        private final String description;
        private final String code;

        MailTemplateType(String description, String code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }

        public String getConfigKey() {
            return MAIL_TEMPLATE_KEY_PREFIX + code;
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        MailConfigDTO config = configApplication.getConfigValueByKey(MAIL_SMTP_CONFIG_KEY, MailConfigDTO.class);
        if (config != null) {
            mailConfiguration.updateMailProperties(config);
        }
    }

    /**
     * 使用模板发送邮件
     *
     * @param type      模板类型
     * @param to        收件人
     * @param variables 模板变量
     */
    public void sendTemplateEmail(MailTemplateType type, String to, Map<String, String> variables) {
        MailTemplateDTO template = getMailTemplate(type);
        if (template == null) {
            throw new ValidationException("邮件模板不存在：" + type.getDescription());
        }

        // 替换主题中的变量
        String subject = replaceVariables(template.getSubject(), variables);
        // 替换内容中的变量
        String content = replaceVariables(template.getContent(), variables);

        MailMessage message = new MailMessage();
        message.setTo(List.of(to));
        message.setSubject(subject);
        message.setContent(content);
        message.setHtml(template.isHtml());

        sendMail(message);
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(key, value);
        }
        return result;
    }

    /**
     * 更新邮件模板
     */
    public void updateMailTemplate(MailTemplateType type, MailTemplateDTO template) {
        if (template != null) {
            configApplication.updateConfig(type.getConfigKey(), JSON.toJSONString(template));
        }
    }

    /**
     * 获取邮件模板
     */
    public MailTemplateDTO getMailTemplate(MailTemplateType type) {
        return configApplication.getConfigValueByKey(type.getConfigKey(), MailTemplateDTO.class);
    }

    public void sendMail(MailMessage message) {
        MailConfigDTO config = configApplication.getConfigValueByKey(MAIL_SMTP_CONFIG_KEY, MailConfigDTO.class);
        if (config == null) {
            throw new ValidationException("邮件配置未初始化");
        }
        // 检查收件人是否在黑名单中或不在白名单中
        EmailListConfigDTO emailListConfig = getEmailListConfig();
        if (emailListConfig != null) {
            validateEmailAddresses(message.getTo(), emailListConfig);
            if (message.getCc() != null) {
                validateEmailAddresses(message.getCc(), emailListConfig);
            }
            if (message.getBcc() != null) {
                validateEmailAddresses(message.getBcc(), emailListConfig);
            }
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // 设置UTF-8编码，并支持多部分消息
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            String from = message.getFrom();
            if (!StringUtils.hasText(from)) {
                from = config.getDefaultFrom() + "<" + config.getUsername() + ">";
            }

            helper.setFrom(from);
            helper.setTo(message.getTo().toArray(new String[0]));
            if (message.getCc() != null && !message.getCc().isEmpty()) {
                helper.setCc(message.getCc().toArray(new String[0]));
            }

            if (message.getBcc() != null && !message.getBcc().isEmpty()) {
                helper.setBcc(message.getBcc().toArray(new String[0]));
            }

            helper.setSubject(message.getSubject());
            helper.setText(message.getContent(), message.isHtml());
            mailSender.send(mimeMessage);
            log.info("Successfully sent email to {}", String.join(",", message.getTo()));
        } catch (MessagingException | MailSendException e) {
            log.error("Failed to send email", e);
            String friendlyMessage = getFriendlyMailErrorMessage(e);
            throw new ValidationException(friendlyMessage);
        }
    }

    /**
     * 获取友好的邮件错误提示信息
     *
     * @param e 邮件相关异常
     * @return 友好的错误提示
     */
    private String getFriendlyMailErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "发送邮件时发生未知错误";
        }

        // 垃圾邮件拦截
        if (message.contains("ANTISPAM") || message.contains("suspected content") || 
            message.contains("spam") || message.contains("Reject by content")) {
            return "邮件内容被服务器判定为垃圾邮件，请检查：\n" +
                   "1. 是否包含敏感词或营销用语\n" +
                   "2. 是否存在过多链接或图片\n" +
                   "3. 邮件主题是否合规\n" +
                   "4. 发件人地址是否有信誉度";
        }

        // 身份验证失败
        if (message.contains("535") || message.contains("Authentication failed")) {
            return "邮箱账号或密码错误，请检查SMTP配置";
        }

        // 连接超时
        if (message.contains("connection timeout") || message.contains("ConnectException")) {
            return "连接邮件服务器超时，请检查网络连接或SMTP服务器配置";
        }

        // 无效的收件人
        if (message.contains("550") || message.contains("Invalid recipient")) {
            return "收件人地址无效，请检查收件人邮箱地址是否正确";
        }

        // 邮箱容量已满
        if (message.contains("552") || message.contains("Mailbox full")) {
            return "收件人邮箱容量已满，请联系收件人处理";
        }

        // 发送频率限制
        if (message.contains("554") || message.contains("rate limit exceeded")) {
            return "发送频率超出限制，请稍后重试";
        }

        // DNS解析错误
        if (message.contains("UnknownHostException")) {
            return "无法解析邮件服务器地址，请检查SMTP服务器配置";
        }

        // SSL/TLS错误
        if (message.contains("SSLException") || message.contains("SSL")) {
            return "SSL/TLS连接失败，请检查邮件服务器的安全连接配置";
        }

        // 邮件大小超限
        if (message.contains("MessageSizeLimitExceededException") || message.contains("message size exceeds")) {
            return "邮件内容过大，请减小邮件大小后重试";
        }

        // 默认错误信息
        return "发送邮件失败：" + e.getMessage();
    }

    private void validateEmailAddresses(List<String> emails, EmailListConfigDTO config) {
        if (config.getAllowedDomains() != null && !config.getAllowedDomains().isEmpty()) {
            for (String email : emails) {
                String domain = extractDomain(email);
                if (!config.getAllowedDomains().contains(domain)) {
                    throw new ValidationException("当前邮件域名不在白名单中：" + domain);
                }
            }
        }

        if (config.getBlockedDomains() != null && !config.getBlockedDomains().isEmpty()) {
            for (String email : emails) {
                String domain = extractDomain(email);
                if (config.getBlockedDomains().contains(domain)) {
                    throw new ValidationException("当前邮件域名在黑名单中：" + domain);
                }
            }
        }
    }

    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            throw new ValidationException("无效的邮件地址格式：" + email);
        }
        return email.substring(email.lastIndexOf("@") + 1).toLowerCase();
    }

    public void updateSmtpConfig(MailConfigDTO config) {
        if (config != null) {
            configApplication.updateConfig(MAIL_SMTP_CONFIG_KEY, JSON.toJSONString(config));
            mailConfiguration.updateMailProperties(config);
        }
    }

    public MailConfigDTO getSmtpConfig() {
        return configApplication.getConfigValueByKey(MAIL_SMTP_CONFIG_KEY, MailConfigDTO.class);
    }

    /**
     * 更新邮件域名名单配置
     */
    public void updateEmailListConfig(EmailListConfigDTO config) {
        if (config != null) {
            // 确保集合不为null
            if (config.getAllowedDomains() == null) {
                config.setAllowedDomains(new HashSet<>());
            }
            if (config.getBlockedDomains() == null) {
                config.setBlockedDomains(new HashSet<>());
            }

            // 转换为小写并移除可能的@前缀
            Set<String> normalizedAllowed = new HashSet<>();
            config.getAllowedDomains().forEach(domain ->
                    normalizedAllowed.add(normalizeDomain(domain)));
            config.setAllowedDomains(normalizedAllowed);

            Set<String> normalizedBlocked = new HashSet<>();
            config.getBlockedDomains().forEach(domain ->
                    normalizedBlocked.add(normalizeDomain(domain)));
            config.setBlockedDomains(normalizedBlocked);

            configApplication.updateConfig(MAIL_LIST_CONFIG_KEY, JSON.toJSONString(config));
        }
    }

    private String normalizeDomain(String domain) {
        if (domain == null) {
            return null;
        }
        domain = domain.toLowerCase();
        if (domain.startsWith("@")) {
            domain = domain.substring(1);
        }
        return domain;
    }

    /**
     * 获取邮件域名名单配置
     */
    public EmailListConfigDTO getEmailListConfig() {
        EmailListConfigDTO config = configApplication.getConfigValueByKey(MAIL_LIST_CONFIG_KEY, EmailListConfigDTO.class);
        if (config == null) {
            config = new EmailListConfigDTO();
            config.setAllowedDomains(new HashSet<>());
            config.setBlockedDomains(new HashSet<>());
        }
        return config;
    }

    /**
     * 添加允许的邮件域名
     */
    public void addAllowedDomain(String domain) {
        EmailListConfigDTO config = getEmailListConfig();
        config.getAllowedDomains().add(normalizeDomain(domain));
        updateEmailListConfig(config);
    }

    /**
     * 移除允许的邮件域名
     */
    public void removeAllowedDomain(String domain) {
        EmailListConfigDTO config = getEmailListConfig();
        config.getAllowedDomains().remove(normalizeDomain(domain));
        updateEmailListConfig(config);
    }

    /**
     * 添加禁止的邮件域名
     */
    public void addBlockedDomain(String domain) {
        EmailListConfigDTO config = getEmailListConfig();
        config.getBlockedDomains().add(normalizeDomain(domain));
        updateEmailListConfig(config);
    }

    /**
     * 移除禁止的邮件域名
     */
    public void removeBlockedDomain(String domain) {
        EmailListConfigDTO config = getEmailListConfig();
        config.getBlockedDomains().remove(normalizeDomain(domain));
        updateEmailListConfig(config);
    }
}
