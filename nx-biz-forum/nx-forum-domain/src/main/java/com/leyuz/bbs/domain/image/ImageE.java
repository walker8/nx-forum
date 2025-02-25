package com.leyuz.bbs.domain.image;

import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import lombok.Data;

import java.time.LocalDateTime;

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