package com.leyuz.uc.log.dto;

import com.alibaba.cola.dto.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 用户日志查询对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLogQuery extends Query {
    @Serial
    private static final long serialVersionUID = 5798426745989251123L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 日志类型
     */
    private Integer logType;

    /**
     * 获取日志类型值对象
     */
    public LogTypeV getLogTypeV() {
        return logType != null ? LogTypeV.fromCode(logType) : null;
    }

    /**
     * 当前页数
     */
    private Integer pageNo;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 排序字段
     */
    private String orderBy;
} 