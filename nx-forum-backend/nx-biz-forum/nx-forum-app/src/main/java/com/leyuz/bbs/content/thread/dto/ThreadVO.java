package com.leyuz.bbs.content.thread.dto;

import com.alibaba.cola.dto.DTO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ThreadVO extends DTO {

    @Serial
    private static final long serialVersionUID = 3683737567675641968L;
    /**
     * 主题id （唯一）
     */
    @TableId(value = "thread_id", type = IdType.AUTO)
    private Long threadId;

    /**
     * 版块ID
     */
    private Integer forumId;
    private String forumName;
    private String forumNickName;

    /**
     * 分类 id 0 是未分类
     */
    private Integer categoryId;

    /**
     * 是否置顶 0 否 1 是 置顶了就不再显示了
     */
    private Integer top;

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
    private Long authorId;
    private String authorName;
    private String avatarUrl;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private Long updateAuthorId;
    private String updateAuthorName;
    private String updateAvatarUrl;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updateTime;

    /**
     * 主题名称
     */
    private String subject;

    /**
     * 主题简介
     */
    private String brief;

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
    private Integer collections;

    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    private Byte commentOrder;

    /**
     * 正文中的图片数量
     */
    private Integer imageCount;
    private List<String> images;

    public void setProperty(ThreadPropertyV property) {
        if (property == null) {
            return;
        }
        setTop(property.getTop());
        setDigest(property.isDigest());
        setBlocked(property.isBlocked());
        setClosed(property.isClosed());
        setRecommendProperty(property.isRecommended());
    }

    public void setRecommendProperty(boolean recommended) {
    }
}
