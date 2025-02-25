package com.leyuz.bbs.content.thread.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorVO {
    private Long authorId;
    private String authorName;
    private String avatarUrl;
    private String intro;
    private Integer threads;
    private Integer comments;
    private Integer fans;
    private Boolean followed;
}