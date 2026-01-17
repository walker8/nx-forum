package com.leyuz.bbs.content.comment;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.common.exception.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.leyuz.bbs.common.utils.HtmlUtils.convertToSafeHtml;

@Getter
@NoArgsConstructor
public class CommentE extends BaseComment {

    private DocTypeV docType;

    /**
     * 楼中楼数
     */
    private Integer replyCount;

    /**
     * 正文中的图片，分隔符为#
     */
    private List<String> images;

    public String getMessageHtml() {
        String html = "";
        if (DocTypeV.TEXT.equals(docType)) {
            html = convertToSafeHtml(message, true);
        }
        html = appendImagesToHtml(html);
        return html;
    }

    public String getMessageHtmlWithoutImages() {
        StringBuilder html;
        if (DocTypeV.TEXT.equals(docType)) {
            html = new StringBuilder(convertToSafeHtml(message, false));
        } else {
            html = new StringBuilder(message);
        }
        if (!CollectionUtils.isEmpty(images)) {
            images.forEach(image -> html.append("[图片]"));
        }
        return html.toString();
    }

    private String appendImagesToHtml(String html) {
        if (!CollectionUtils.isEmpty(images)) {
            StringBuilder imagesHtml = new StringBuilder();
            for (String image : images) {
                String thumbImage = image;
                imagesHtml.append("<img src=\"" + thumbImage + "\" originalsrc=\"" + image + "\" />");
            }
            html = html + "<div class=\"gallery\">" + imagesHtml + "</div>";
        }
        return html;
    }

    @Builder
    public CommentE(Long threadId, Long commentId, Integer forumId, String userIp, String userAgent, String terminalType, String platform, Integer likes, AuditStatusV auditStatus, String auditReason, String message, LocalDateTime createTime, LocalDateTime updateTime, Long createBy, Long updateBy, DocTypeV docType, Integer replyCount, List<String> images) {
        super(threadId, commentId, forumId, userIp, userAgent, terminalType, platform, likes, auditStatus, auditReason, message, createTime, updateTime, createBy, updateBy);
        this.docType = docType;
        this.replyCount = replyCount;
        this.images = images;
    }

    @Override
    public void create() {
        super.create();
        replyCount = 0;
    }

    @Override
    protected void checkMessage() {
        if (StringUtils.isBlank(message) && CollectionUtils.isEmpty(images)) {
            throw new ValidationException("评论不能为空！");
        }
        if (StringUtils.isNotBlank(message) && message.length() > 1000) {
            throw new ValidationException("评论内容过长！");
        }
    }
}
