package com.leyuz.bbs.system.page;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("bbs_custom_pages")
public class CustomPagePO extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 2976615888531190992L;
    @TableId(type = IdType.AUTO)
    private Long pageId;
    private String pageName;
    private String pageCode;
    private Integer pageStatus;
    private Integer accessLevel;
} 