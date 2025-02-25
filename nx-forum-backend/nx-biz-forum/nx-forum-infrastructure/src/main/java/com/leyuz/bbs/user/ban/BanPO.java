package com.leyuz.bbs.user.ban;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bbs_user_bans")
public class BanPO extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4066765524527813448L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer forumId;
    private String reason;
    private LocalDateTime expireTime;
    private Integer operationType;
} 