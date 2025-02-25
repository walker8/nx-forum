package com.leyuz.bbs.user.property;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("bbs_user_properties")
public class ForumUserPropertyPO extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 195173393162813587L;
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 发帖数
     */
    private Integer threads;

    /**
     * 评论数
     */
    private Integer comments;

    /**
     * 粉丝数
     */
    private Integer fans;

    /**
     * 积分
     */
    private Integer credits;

    /**
     * 金币
     */
    private Integer golds;
} 