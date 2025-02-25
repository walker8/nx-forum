package com.leyuz.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeUtils {

    /**
     * 格式化时间
     *
     * @param dateTime
     * @return
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        // 获取当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, nowTime);
        long days = duration.toDays();
        if (days > 30) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return dateTime.format(dtf);
        }
        if (days > 0) {
            return days + "天前";
        }
        long hours = duration.toHours();
        if (hours > 0) {
            return hours + "小时前";
        }
        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + "分钟前";
        }
        return "刚刚";
    }

}
