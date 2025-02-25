package com.leyuz.bbs.system.page;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

@Data
@TableName("bbs_custom_page_contents")
public class CustomPageContentPO extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long contentId;
    private Long pageId;
    private String content;
    private Integer version;
}