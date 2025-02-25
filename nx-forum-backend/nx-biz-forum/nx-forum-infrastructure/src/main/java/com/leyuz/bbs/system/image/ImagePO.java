package com.leyuz.bbs.system.image;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("bbs_images")
public class ImagePO extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 629612680924719536L;
    @TableId(type = IdType.AUTO)
    private Long imageId;
    private String imagePath;
    private Integer imageType;
    private Long fileSize;
    private String fileHash;
    private String fileExt;
    private Integer storageType;
} 