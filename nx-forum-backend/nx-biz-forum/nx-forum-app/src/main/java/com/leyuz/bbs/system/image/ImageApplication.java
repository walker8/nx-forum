package com.leyuz.bbs.system.image;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.system.attach.AttachApplication;
import com.leyuz.bbs.system.image.dto.ImageCmd;
import com.leyuz.bbs.system.image.dto.ImagePageQuery;
import com.leyuz.bbs.system.image.dto.ImageVO;
import com.leyuz.bbs.system.image.dto.constant.ImageTypeV;
import com.leyuz.bbs.system.image.dto.constant.StorageTypeV;
import com.leyuz.bbs.system.image.model.ImageE;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageApplication {

    private final ImageMapper imageMapper;
    private final UserApplication userApplication;

    public CustomPage<ImageVO> queryPage(ImagePageQuery query) {
        Page<ImagePO> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<ImagePO> wrapper = new LambdaQueryWrapper<>();

        if (query.getImageType() != null) {
            wrapper.eq(ImagePO::getImageType, query.getImageType());
        }
        if (query.getStorageType() != null) {
            wrapper.eq(ImagePO::getStorageType, query.getStorageType());
        }
        if (query.getCreateBy() != null) {
            wrapper.eq(ImagePO::getCreateBy, query.getCreateBy());
        }

        imageMapper.selectPage(page, wrapper);

        return DataBaseUtils.createCustomPage(page, po -> toImageVO(poToEntity(po)));
    }

    public void deleteBatch(List<Long> imageIds) {
        for (Long imageId : imageIds) {
            ImagePO imagePO = imageMapper.selectById(imageId);
            if (imagePO != null) {
                ImageE imageE = poToEntity(imagePO);
                SpringUtil.getBean(AttachApplication.class).deleteImage(imageE.getImagePath(), imageE.getStorageType());
                imageMapper.deleteById(imageId);
            }
        }
    }

    /**
     * 新增图片
     */
    public void save(ImageCmd imageCmd) {
        ImagePO imagePO = new ImagePO();
        BeanUtils.copyProperties(imageCmd, imagePO);
        if (imageCmd.getImageType() != null) {
            imagePO.setImageType(imageCmd.getImageType().getValue());
        }
        if (imageCmd.getStorageType() != null) {
            imagePO.setStorageType(imageCmd.getStorageType().getValue());
        }
        // 设置基础字段（create_time, update_time 等）
        BaseEntityUtils.setCreateBaseEntity(imagePO);
        imageMapper.insert(imagePO);
    }

    /**
     * 根据图片类型和hash值查询图片
     */
    public ImageVO getImage(String fileHash, ImageTypeV imageType) {
        LambdaQueryWrapper<ImagePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImagePO::getFileHash, fileHash);
        if (imageType != null) {
            wrapper.eq(ImagePO::getImageType, imageType.getValue());
        }
        ImagePO imagePO = imageMapper.selectOne(wrapper);
        return toImageVO(poToEntity(imagePO));
    }

    /**
     * PO转换为实体对象
     */
    private ImageE poToEntity(ImagePO po) {
        if (po == null) {
            return null;
        }
        ImageE entity = new ImageE();
        BeanUtils.copyProperties(po, entity);
        entity.setImageType(ImageTypeV.of(po.getImageType()));
        entity.setStorageType(StorageTypeV.of(po.getStorageType()));
        return entity;
    }

    /**
     * 实体对象转换为VO
     */
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