package com.leyuz.bbs.system.image.model;

import com.leyuz.bbs.system.image.dto.constant.ImageTypeV;
import com.leyuz.bbs.system.image.dto.constant.StorageTypeV;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片实体模型
 */
@Data
public class ImageE {
    private Long imageId;
    private String imagePath;
    private ImageTypeV imageType;
    private Long fileSize;
    private String fileHash;
    private String fileExt;
    private StorageTypeV storageType;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
    private Boolean isDeleted;
}

