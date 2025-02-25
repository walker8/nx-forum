package com.leyuz.bbs.ban.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BanCreateCmd {
    private Long userId;
    private Integer forumId;
    private String reason;
    private LocalDateTime expireTime;
} 