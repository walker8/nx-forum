package com.leyuz.bbs.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BanVO {
    private Long id;
    // 禁言用户名
    private String userName;
    private Long userId;
    // 操作人用户名
    private String operationUserName;
    private Integer forumId;
    // 版块名称
    private String forumName;
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime expireTime;
    private Integer operationType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
} 