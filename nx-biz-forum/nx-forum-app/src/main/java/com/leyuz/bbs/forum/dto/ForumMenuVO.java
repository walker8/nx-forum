package com.leyuz.bbs.forum.dto;

import lombok.Data;

import java.util.List;

@Data
public class ForumMenuVO {
    private String defaultForumName;
    private List<ForumMenuItemVO> records;
}
