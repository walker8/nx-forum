package com.leyuz.bbs.image;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.domain.image.ImageE;
import com.leyuz.bbs.domain.image.gateway.ImageGateway;
import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import com.leyuz.bbs.image.mybatis.ImageMapper;
import com.leyuz.bbs.image.mybatis.ImagePO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageGatewayImpl implements ImageGateway {

    private final ImageMapper imageMapper;

    @Override
    public ImageE save(ImageE image) {
        ImagePO po = toImagePO(image);
        BaseEntityUtils.setCreateBaseEntity(po);
        imageMapper.insert(po);
        return toImageE(po);
    }

    @Override
    public void delete(Long imageId) {
        imageMapper.deleteById(imageId);
    }

    @Override
    public ImageE update(ImageE image) {
        ImagePO po = toImagePO(image);
        BaseEntityUtils.setUpdateBaseEntity(po);
        imageMapper.updateById(po);
        return toImageE(po);
    }

    @Override
    public ImageE findById(Long imageId) {
        ImagePO po = imageMapper.selectById(imageId);
        return toImageE(po);
    }

    @Override
    public ImageE findByFileHashAndType(String fileHash, ImageTypeV imageType) {
        LambdaQueryWrapper<ImagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImagePO::getFileHash, fileHash)
                .eq(ImagePO::getImageType, imageType.getValue());
        ImagePO po = imageMapper.selectOne(wrapper);
        return toImageE(po);
    }

    @Override
    public CustomPage<ImageE> queryPage(Integer pageNo, Integer pageSize, ImageTypeV imageType,
                                        StorageTypeV storageType, Long createBy) {
        LambdaQueryWrapper<ImagePO> wrapper = new LambdaQueryWrapper<>();
        if (imageType != null) {
            wrapper.eq(ImagePO::getImageType, imageType.getValue());
        }
        if (storageType != null) {
            wrapper.eq(ImagePO::getStorageType, storageType.getValue());
        }
        if (createBy != null) {
            wrapper.eq(ImagePO::getCreateBy, createBy);
        }
        wrapper.orderByDesc(ImagePO::getCreateTime);

        Page<ImagePO> page = imageMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return DataBaseUtils.createCustomPage(page, this::toImageE);
    }

    private ImageE toImageE(ImagePO po) {
        if (po == null) {
            return null;
        }
        ImageE e = new ImageE();
        BeanUtils.copyProperties(po, e);
        e.setImageType(ImageTypeV.of(po.getImageType()));
        e.setStorageType(StorageTypeV.of(po.getStorageType()));
        return e;
    }

    private ImagePO toImagePO(ImageE e) {
        if (e == null) {
            return null;
        }
        ImagePO po = new ImagePO();
        BeanUtils.copyProperties(e, po);
        if (e.getImageType() != null) {
            po.setImageType(e.getImageType().getValue());
        }
        if (e.getStorageType() != null) {
            po.setStorageType(e.getStorageType().getValue());
        }
        return po;
    }
} 