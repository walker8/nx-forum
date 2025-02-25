package com.leyuz.bbs.forum.access;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

@Data
@TableName("bbs_forum_access")
public class ForumAccessPO extends BaseEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Integer forumId;
    
    private String roleKey;
    
    private String perms;
    
    private String remark;
} 