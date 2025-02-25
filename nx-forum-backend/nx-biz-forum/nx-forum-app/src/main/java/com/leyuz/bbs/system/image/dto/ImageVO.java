package com.leyuz.bbs.system.image.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "图片信息")
public class ImageVO {
    @Schema(description = "图片ID")
    private Long imageId;

    @Schema(description = "图片路径")
    private String imagePath;

    @Schema(description = "图片类型（0帖子图片 1用户头像）")
    private Integer imageType;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件hash值")
    private String fileHash;

    @Schema(description = "文件后缀")
    private String fileExt;

    @Schema(description = "存储类型（0本地 1阿里云OSS 2腾讯云COS）")
    private Integer storageType;

    @Schema(description = "创建者")
    private Long authorId;
    @Schema(description = "用户名")
    private String authorName;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
} 