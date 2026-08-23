package com.leyuz.bbs.common.dataobject;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * AI 审核历史主题帖条目（标题 + 概要 + 发帖时间）
 *
 * @author Walker
 */
@Value
public class ThreadHistoryItemV {
    /**
     * 主题帖标题
     */
    private String title;
    /**
     * 主题帖概要（对应 bbs_threads.brief，自动从正文截取）
     */
    private String summary;
    /**
     * 发帖时间
     */
    private LocalDateTime createTime;
}