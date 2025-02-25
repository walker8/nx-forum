package com.leyuz.bbs.forum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumUserRoleCmd {

    private Long userId;

    private Integer forumId;

    private String roleKey;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
} 