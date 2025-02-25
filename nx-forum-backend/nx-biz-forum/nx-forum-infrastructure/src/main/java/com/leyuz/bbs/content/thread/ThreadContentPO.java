package com.leyuz.bbs.content.thread;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
@TableName("bbs_thread_contents")
@Data
public class ThreadContentPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8600796876086091618L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主题id
     */
    private Long threadId;

    private String content;

    /**
     * 备注
     */
    private String remark;
}
