package com.leyuz.bbs.image.dto;

import com.alibaba.cola.dto.Command;
import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;

@Data
@Builder
public class ImageCmd extends Command {
    @Serial
    private static final long serialVersionUID = 8100050857320367105L;
    private String imagePath;
    private ImageTypeV imageType;
    private Long fileSize;
    private String fileHash;
    private String fileExt;
    private StorageTypeV storageType;
}
