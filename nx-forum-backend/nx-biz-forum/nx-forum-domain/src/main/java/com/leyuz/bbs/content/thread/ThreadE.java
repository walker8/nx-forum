package com.leyuz.bbs.content.thread;

import cn.hutool.core.text.CharSequenceUtil;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.common.utils.MarkdownUtils;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.UserAgentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadE {
    private Long threadId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 分类 id 0 是未分类
     */
    private Integer categoryId;

    /**
     * 属性
     */
    private ThreadPropertyV property;

    /**
     * 发帖时用户ip地址
     */
    private String userIp;

    /**
     * 客户端useragent
     */
    private String userAgent;

    /**
     * 终端类型（PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台（Windows/Mac/Android/iPhone等）
     */
    private String platform;

    /**
     * 主题名称
     */
    private String subject;

    /**
     * 主题简介，自动提取自主题内容
     */
    private String brief;

    /**
     * 类型，0: html 1: text 2:markdown 3: ubb 一般就支持html和markdown
     */
    private DocTypeV docType;

    /**
     * 查看次数
     */
    private Integer views;

    /**
     * 评论数
     */
    private Integer comments;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 不喜欢数
     */
    private Integer dislikes;

    /**
     * 收藏数
     */
    private Integer collections;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝 3 忽略审核
     */
    private AuditStatusV auditStatus;
    private String auditReason;

    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    private CommentOrderV commentOrder;

    /**
     * 最近参与的用户（最后回帖的用户名）
     */
    private Long lastCommentUserId;

    /**
     * 最后回复时间
     */
    private LocalDateTime lastCommentTime;

    /**
     * 正文中的图片，分隔符为#
     */
    private List<String> images;
    /**
     * 正文中的图片数量
     */
    private Integer imageCount;

    private String content;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    private Document document;

    /**
     * 创建主题帖
     */
    public void create() {
        // 初始化
        init();
        userIp = HeaderUtils.getIp();
        userAgent = HeaderUtils.getUserAgent();

        // 简化：直接调用UserAgentUtils获取完整客户端信息
        String appVersion = HeaderUtils.getAppVersion();
        UserClientInfo clientInfo = UserAgentUtils.getClientInfo(userAgent, appVersion);
        terminalType = clientInfo.getTerminalType();
        platform = clientInfo.getPlatform();

        views = 0;
        comments = 0;
        likes = 0;
        dislikes = 0;
        collections = 0;
        property = new ThreadPropertyV();
        property.init();
        // 最后回复时间默认是创建时间
        lastCommentTime = LocalDateTime.now();
    }

    public void update() {
        // 初始化
        init();
    }

    public ThreadPropertyV getProperty() {
        if (property == null) {
            property = new ThreadPropertyV();
            property.init();
        }
        return property;
    }

    /**
     * 设置审核结果
     *
     * @param auditStatus
     * @param auditReason
     */
    public void setAuditResult(AuditStatusV auditStatus, String auditReason) {
        this.auditStatus = auditStatus;
        this.auditReason = auditReason;
    }

    public void setContent(String content) {
        // 如果 docType 为 MARKDOWN，先将 markdown 转换为 HTML
        if (docType == DocTypeV.MARKDOWN && StringUtils.isNotEmpty(content)) {
            content = MarkdownUtils.convertMarkdownToHtml(content);
        }
        this.content = content;
    }

    public void outputForView() {
        initDocument();
        if (document != null) {
            // 为h1、h2、h3标签增加锚点id
            int headingCount = 0;
            for (int i = 1; i <= 3; i++) {
                Elements headings = document.select("h" + i);
                for (Element heading : headings) {
                    String text = "heading-" + (headingCount++);
                    heading.attr("id", text);
                }
            }
            // 从h6开始向上调整，避免标签冲突
            for (int i = 6; i >= 1; i--) {
                Elements headings = document.select("h" + i);
                for (Element heading : headings) {
                    // 将当前级别的标题改为下一级
                    heading.tagName("h" + (i + 1));
                }
            }

            // 对文档中的图片元素添加原图地址属性
            Elements imgElements = document.getElementsByTag("img");
            for (Element imgElement : imgElements) {
                String sourceUrl = imgElement.attr("src");
                // 原图地址
                imgElement.attr("originalSrc", sourceUrl);
            }

            content = document.body().toString();
        }
    }

    /**
     * 获取标题，为空时使用简介
     *
     * @return
     */
    public String getTitle() {
        String title = subject;
        if (StringUtils.isEmpty(title)) {
            title = StringUtils.substring(brief, 0, 50);
        }
        if (StringUtils.isEmpty(title)) {
            title = "[图片]";
        }
        return title;
    }

    private void initDocument() {
        if (StringUtils.isNotEmpty(content) && document == null) {
            document = Jsoup.parse(content);
        }
    }

    private void initLink() {
        if (StringUtils.isBlank(content)) {
            return;
        }

        // 查找所有包含 img 的 a 标签
        Elements aTagsWithImg = document.select("a > img");
        // 遍历找到的 a 标签，将 img 移出 a 并删除 a
        for (Element img : aTagsWithImg) {
            Element aTag = img.parent();
            aTag.replaceWith(img);
        }

        // 遍历找到的 a 标签，设置 target 属性为 _blank
        Elements aElements = document.getElementsByTag("a");
        for (Element aElement : aElements) {
            if (StringUtils.isEmpty(aElement.text())) {
                // 移除空链接
                aElement.remove();
            } else {
                aElement.attr("target", "_blank");
            }
        }
    }

    private void initImagesAndBrief() {
        images = new ArrayList<>();
        if (StringUtils.isNotEmpty(content)) {
            // 初始化图片
            Elements imgElements = document.getElementsByTag("img");
            for (Element imgElement : imgElements) {
                String img = imgElement.attr("file");
                if (StringUtils.isEmpty(img)) {
                    img = imgElement.attr("src");
                    if (StringUtils.isNotEmpty(img)) {
                        images.add(img);
                    }
                }
            }
            // 初始化主题简介
            brief = CharSequenceUtil.sub(document.text(), 0, 255);
        }
        imageCount = images.size();
    }

    private void init() {
        // 初始化文档
        initDocument();
        // 格式化文档样式
        optimizeDocument(document);
        // 初始化a标签
        initLink();
        // 移除script标签
        removeScript();
        // 初始化图片和简介
        initImagesAndBrief();
        // 校验标题和内容
        checkSubjectAndContent();
        if (document != null) {
            content = document.body().toString();
        }
    }

    /**
     * 格式化文档样式
     *
     * @param document
     */
    private void optimizeDocument(Document document) {
        if (document != null) {
            // 删除svg标签
            document.select("svg").remove();
            // 标题去除a标签
            document.select("h1, h2, h3, h4, h5, h6").forEach(heading -> heading.select("a").unwrap());
            // 对标题进行重新排序
            int maxHeadingLevel = 6;
            for (int i = 1; i <= 6; i++) {
                if (!document.select("h" + i).isEmpty()) {
                    maxHeadingLevel = i;
                    break;
                }
            }
            if (maxHeadingLevel > 1) {
                // 如果最小标题级别大于1，则进行重新排序（如h2->h1, h3->h2, h4->h3）
                // 先收集所有需要修改的标题元素，避免动态查询导致重复修改
                List<Element> allHeadings = new ArrayList<>();
                for (int i = maxHeadingLevel; i <= 6; i++) {
                    allHeadings.addAll(document.select("h" + i));
                }
                // 对每个标题元素进行重新排序
                for (Element heading : allHeadings) {
                    String tagName = heading.tagName();
                    int level = Integer.parseInt(tagName.substring(1)); // h2 -> 2
                    int newLevel = level - maxHeadingLevel + 1;
                    heading.tagName("h" + newLevel);
                }
            }
            // 格式化code块
            document.getElementsByTag("code").forEach(code -> code.html(code.text()));
            // 如果pre标签没有class属性，则添加class属性
            document.getElementsByTag("pre").forEach(pre -> {
                if (!pre.hasAttr("class")) {
                    pre.attr("class", "language-bash");
                }
            });
            // 去除img标签里除src和alt外的所有属性
            document.select("img").forEach(img -> {
                String src = img.attr("src");
                String alt = img.attr("alt");
                img.clearAttributes();
                img.attr("src", src);
                if (!alt.isEmpty()) {
                    img.attr("alt", alt);
                }
            });
        }
    }

    private void removeScript() {
        if (document != null) {
            // 选择所有的 script 标签
            Elements scripts = document.select("script");

            // 移除 script 标签
            for (Element script : scripts) {
                script.remove();
            }
        }
    }

    private void checkSubjectAndContent() {
        if (StringUtils.isNotEmpty(subject) && subject.length() > 80) {
            throw new ValidationException("标题不可以超过80字符！");
        }
        if (StringUtils.isEmpty(brief)) {
            if (CharSequenceUtil.isBlank(subject)) {
                throw new ValidationException("标题和内容不能都为空字符串！");
            } else {
                if (CollectionUtils.isEmpty(images)) {
                    // 没有图片也没有内容，只有标题的情况
                    throw new ValidationException("请补充下内容描述！");
                }
            }
        }
    }
}
