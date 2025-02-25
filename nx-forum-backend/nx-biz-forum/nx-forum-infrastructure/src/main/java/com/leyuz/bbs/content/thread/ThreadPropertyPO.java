package com.leyuz.bbs.content.thread;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@TableName("bbs_thread_properties")
@Data
public class ThreadPropertyPO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主题ID
     */
    private Long threadId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 类型
     */
    private Integer propertyType;

    /**
     * 属性
     */
    private Integer attribute;
}
