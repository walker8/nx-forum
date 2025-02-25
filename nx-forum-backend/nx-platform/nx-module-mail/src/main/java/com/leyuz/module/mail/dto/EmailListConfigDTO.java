package com.leyuz.module.mail.dto;

import lombok.Data;

import java.util.Set;

@Data
public class EmailListConfigDTO {

    //允许的邮件域名列表（如：example.com）
    private Set<String> allowedDomains;

    // 禁止的邮件域名列表（如：blocked.com）
    private Set<String> blockedDomains;
} 