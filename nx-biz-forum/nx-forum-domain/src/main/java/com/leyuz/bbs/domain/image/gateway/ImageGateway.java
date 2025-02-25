package com.leyuz.bbs.domain.image.gateway;

import com.leyuz.bbs.domain.image.ImageE;
import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import com.leyuz.common.mybatis.CustomPage;

public interface ImageGateway {

    ImageE save(ImageE image);

    void delete(Long imageId);

    ImageE update(ImageE image);

    ImageE findById(Long imageId);

    ImageE findByFileHashAndType(String fileHash, ImageTypeV imageType);

    CustomPage<ImageE> queryPage(Integer pageNo, Integer pageSize, ImageTypeV imageType,
                                 StorageTypeV storageType, Long createBy);
} 