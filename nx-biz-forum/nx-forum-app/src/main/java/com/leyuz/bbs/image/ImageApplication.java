package com.leyuz.bbs.image;

import cn.hutool.extra.spring.SpringUtil;
import com.leyuz.bbs.attach.AttachApplication;
import com.leyuz.bbs.domain.image.ImageE;
import com.leyuz.bbs.domain.image.gateway.ImageGateway;
import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import com.leyuz.bbs.image.dto.ImageCmd;
import com.leyuz.bbs.image.dto.ImagePageQuery;
import com.leyuz.bbs.image.dto.ImageVO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.user.UserApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageApplication {

    private final ImageGateway imageGateway;
    private final UserApplication userApplication;

    public CustomPage<ImageVO> queryPage(ImagePageQuery query) {
        CustomPage<ImageE> page = imageGateway.queryPage(
                query.getPageNo(),
                query.getPageSize(),
                ImageTypeV.of(query.getImageType()),
                StorageTypeV.of(query.getStorageType()),
                query.getCreateBy()
        );
        return CustomPage.<ImageVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .records(page.getRecords().stream().map(this::toImageVO).toList())
                .build();
    }

    public void deleteBatch(List<Long> imageIds) {
        for (Long imageId : imageIds) {
            ImageE imageE = imageGateway.findById(imageId);
            if (imageE != null) {
                SpringUtil.getBean(AttachApplication.class).deleteImage(imageE.getImagePath(), imageE.getStorageType());
                imageGateway.delete(imageId);
            }
        }
    }

    /**
     * 新增图片
     */
    public void save(ImageCmd imageCmd) {
        ImageE imageE = new ImageE();
        BeanUtils.copyProperties(imageCmd, imageE);
        imageGateway.save(imageE);
    }

    /**
     * 根据图片类型和hash值查询图片
     */
    public ImageVO getImage(String fileHash, ImageTypeV imageType) {
        ImageE imageE = imageGateway.findByFileHashAndType(fileHash, imageType);
        return toImageVO(imageE);
    }

    private ImageVO toImageVO(ImageE e) {
        if (e == null) {
            return null;
        }
        ImageVO vo = new ImageVO();
        BeanUtils.copyProperties(e, vo);
        if (e.getImageType() != null) {
            vo.setImageType(e.getImageType().getValue());
        }
        if (e.getStorageType() != null) {
            vo.setStorageType(e.getStorageType().getValue());
        }
        if (e.getCreateBy() != null) {
            vo.setAuthorId(e.getCreateBy());
            UserE userE = userApplication.getByIdFromCache(e.getCreateBy());
            vo.setAuthorName(userE.getUserName());
        }
        return vo;
    }
} 