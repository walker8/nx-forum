package com.leyuz.bbs.system.audit.context;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 发帖人信息（规则/AI 入参）
 *
 * @author Walker
 */
@Getter
@Builder
public class PosterInfo {
    private Long userId;
    private String userName;
    /**
     * 注册时间
     */
    private LocalDateTime registerTime;
    /**
     * 注册天数
     */
    private long registerDays;
    /**
     * 注册/活跃地（IP 归属地）
     */
    private String location;
    /**
     * 最近活跃时间
     */
    private LocalDateTime lastActiveDate;
    /**
     * 历史主题帖数
     */
    private long postCount;
    /**
     * 历史评论/回复数
     */
    private long commentCount;
}
