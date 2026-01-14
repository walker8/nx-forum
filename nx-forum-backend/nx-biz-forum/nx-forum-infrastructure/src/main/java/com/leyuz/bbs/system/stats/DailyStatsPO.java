package com.leyuz.bbs.system.stats;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日统计汇总实体类
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bbs_daily_stats")
public class DailyStatsPO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    @TableId(value = "stat_id", type = IdType.AUTO)
    private Long statId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 独立IP访问数
     */
    private Integer uniqueIpCount;

    /**
     * 游客独立IP数
     */
    private Integer guestIpCount;

    /**
     * 登录用户独立IP数
     */
    private Integer userIpCount;

    /**
     * 发帖数量
     */
    private Integer threadCount;

    /**
     * 回帖数量
     */
    private Integer commentCount;

    /**
     * 终端类型（ALL/PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台（ALL表示全部）
     */
    private String platform;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
