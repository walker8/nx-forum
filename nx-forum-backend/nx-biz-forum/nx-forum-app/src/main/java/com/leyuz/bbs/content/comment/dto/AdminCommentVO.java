package com.leyuz.bbs.content.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminCommentVO {
    private Long commentId;
    /**
     * 楼中楼数
     */
    private Integer replyCount;

    /**
     * 正文中的图片，分隔符为#
     */
    private List<String> images;
    private Long threadId;

    private Integer forumId;
    private String forumNickName;
    /**
     * 点赞数
     */
    private Integer likes;
    /**
     * 内容
     */
    private String message;
    private String auditReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;

    private Long authorId;
    private String authorName;
    private String avatarUrl;
    private String userIp;
    private String location;
    private String browser;
    private String os;
}
