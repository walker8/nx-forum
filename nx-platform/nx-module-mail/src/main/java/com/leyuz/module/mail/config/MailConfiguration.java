package com.leyuz.module.mail.config;

import com.leyuz.module.mail.dto.MailConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@Slf4j
public class MailConfiguration {
    private JavaMailSenderImpl mailSender;

    public void updateMailProperties(MailConfigDTO config) {
        if (config == null) {
            log.error("Mail configuration is missing in database");
            throw new IllegalStateException("Mail configuration is required");
        } else {
            mailSender.setHost(config.getHost());
            mailSender.setPort(config.getPort());
            mailSender.setUsername(config.getUsername());
            mailSender.setPassword(config.getPassword());
            mailSender.setProtocol(config.getProtocol());
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", config.getAuth());
            props.put("mail.smtp.starttls.enable", config.getStarttlsEnable());
            // 设置邮件编码
            props.put("mail.smtp.encoding", StandardCharsets.UTF_8.name());
            props.put("mail.smtp.charset", StandardCharsets.UTF_8.name());
        }
    }

    @Bean
    public JavaMailSender javaMailSender() {
        if (mailSender == null) {
            mailSender = new JavaMailSenderImpl();
        }

        return mailSender;
    }
}
