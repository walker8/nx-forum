package com.leyuz.bbs.forum.dto;

import lombok.Data;

import java.util.List;

@Data
public class ForumAccessDTO {
    private Long id;
    private String roleKey;
    private String roleName;
    private List<String> perms;
    private String remark;
} 