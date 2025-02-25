package com.leyuz.bbs.content.thread.dto;

import com.alibaba.cola.dto.Command;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

@Schema(description = "帖子内容")
@Data
public class ThreadCmd extends Command {
    @Serial
    private static final long serialVersionUID = -1790165467011940787L;
    private String content;
    private String remark;
    private String subject;
    private DocTypeV contentType;
    private Integer categoryId;
    private Integer forumId;
    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    private CommentOrderV commentOrder;
}
