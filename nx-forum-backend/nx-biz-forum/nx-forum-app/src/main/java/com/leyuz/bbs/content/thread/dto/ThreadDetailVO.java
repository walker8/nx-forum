package com.leyuz.bbs.content.thread.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
public class ThreadDetailVO {
    @Serial
    private static final long serialVersionUID = 898009789904284253L;

    /**
     * 主题id （唯一）
     */
    @TableId(value = "thread_id", type = IdType.AUTO)
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
     * 是否置顶 0 否 1 是 置顶了就不再显示了
     */
    private Boolean top;

    /**
     * 0 正常 1 屏蔽
     */
    private Boolean blocked;

    /**
     * 0 不关闭 1 关闭
     */
    private Boolean closed;

    /**
     * 是否精华 0 否 1 是
     */
    private Boolean digest;

    /**
     * 创建者
     */
    private AuthorVO author;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private AuthorVO updateAuthor;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;

    /**
     * 主题名称
     */
    private String subject;

    /**
     * 类型，0: html 1: text 2:markdown 3: ubb 一般就支持html和markdown
     */
    private Byte docType;

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
    private Boolean liked = false;

    /**
     * 不喜欢数
     */
    private Integer dislikes;

    /**
     * 收藏数
     */
    private Integer collections = 0;
    private Boolean collected = false;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private Byte auditStatus;

    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    private Byte commentOrder;

    /**
     * 正文中的图片数量
     */
    private Integer imageCount;

    private String content;

    private String seoTitle;

    private String seoContent;

    /**
     * 是否已删除（管理员可见）
     */
    private Boolean deleted;
}
