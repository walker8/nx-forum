package com.leyuz.bbs.content.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminCommentReplyVO {
    private Long commentId;
    private Long replyId;
    private Long threadId;
    /**
     * 点赞数
     */
    private Integer likes;
    /**
     * 内容
     */
    private String message;
    private String auditReason;

    private Integer forumId;
    private String forumNickName;

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
