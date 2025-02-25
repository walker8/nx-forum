package com.leyuz.bbs.system.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图片分页查询参数")
public class ImagePageQuery {
    @Schema(description = "图片类型（0帖子图片 1用户头像）")
    private Integer imageType;
    
    @Schema(description = "存储类型（0本地 1阿里云OSS 2腾讯云COS）")
    private Integer storageType;
    
    @Schema(description = "创建者ID")
    private Long createBy;
    
    @Schema(description = "页码")
    private Integer pageNo = 1;
    
    @Schema(description = "每页大小")
    private Integer pageSize = 10;
} 