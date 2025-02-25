package com.leyuz.bbs.interaction.favorite;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@TableName("bbs_user_favorites")
@Builder
public class FavoritePO {
    @Serial
    private static final long serialVersionUID = -6701438159000501L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long threadId;
    /**
     * 创建者
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 