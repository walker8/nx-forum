package com.leyuz.bbs.system.audit.context;

import lombok.Builder;
import lombok.Getter;

/**
 * 内容信息（规则/AI 入参）
 *
 * @author Walker
 */
@Getter
@Builder
public class ContentInfo {
    /**
     * 纯文本内容
     */
    private String text;
    /**
     * 文本长度
     */
    private int length;
    /**
     * 链接总数
     */
    private int linkCount;
    /**
     * 外部链接数（排除白名单后）
     */
    private int externalLinkCount;
    /**
     * 是否包含链接
     */
    private Boolean hasLink;
    /**
     * 图片数量
     */
    private int imageCount;
}
