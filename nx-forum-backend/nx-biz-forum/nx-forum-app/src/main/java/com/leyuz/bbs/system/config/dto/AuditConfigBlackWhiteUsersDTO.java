package com.leyuz.bbs.system.config.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class AuditConfigBlackWhiteUsersDTO {
    // 自动通过白名单用户（用户ID集合）
    private Set<Long> whiteListUsers = new HashSet<>();
    // 黑名单用户（用户ID集合）
    private Set<Long> blackListUsers = new HashSet<>();
} 