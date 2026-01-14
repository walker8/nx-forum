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
 * 每小时访问统计实体类
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("bbs_hourly_stats")
public class HourlyStatsPO {

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
     * 统计小时（0-23）
     */
    private Integer statHour;

    /**
     * 独立IP数
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
     * 终端类型
     */
    private String terminalType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
