package com.leyuz.bbs.attach;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpDownloader;
import com.alibaba.cola.exception.BizException;
import com.leyuz.bbs.domain.image.valueobject.ImageTypeV;
import com.leyuz.bbs.domain.image.valueobject.StorageTypeV;
import com.leyuz.bbs.image.ImageApplication;
import com.leyuz.bbs.image.dto.ImageCmd;
import com.leyuz.bbs.image.dto.ImageVO;
import com.leyuz.common.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachApplication {
    private final ImageApplication imageApplication;

    @Value("${file.upload.path}")
    private String fileUploadPath;

    @Value("${image.compressed.max.size:200}")
    private Integer maxCompressedImageSizeKB;

    private static final Integer MAX_IMAGE_WIDTH = 1200;

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return
     */
    public String uploadImage(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new ValidationException("图片文件不能为空！");
        }
        // 文件大小，单位kb
        long fileSizeKB = file.getSize() / 1024;
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        if (!contentType.startsWith("image")) {
            throw new ValidationException("图片格式不正确！");
        }
        String imageSuffix = CharSequenceUtil.subAfter(contentType, "/", true);
        try {
            // 获取文件的字节
            byte[] bytes = file.getBytes();
            return saveImage(bytes, imageSuffix, fileSizeKB);
        } catch (IOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private String saveImage(byte[] bytes, String imageSuffix, long fileSizeKB) {
        try {
            BufferedImage bufferedImage = ImgUtil.toImage(bytes);
            int height = bufferedImage.getHeight();
            int width = bufferedImage.getWidth();
            if (StringUtils.isEmpty(imageSuffix)) {
                imageSuffix = detectByFileHeader(bytes);
            }
            String fileHash = DigestUtil.md5Hex(bytes);
            ImageVO imageVO = imageApplication.getImage(fileHash, ImageTypeV.THREAD);
            if (imageVO != null) {
                return imageVO.getImagePath();
            }
            String fileName = "/" + fileHash + "." + imageSuffix;
            String filePath = "/uploads/image/" + DateUtil.format(DateUtil.date(), "yyyyMM");
            createDirPath(fileUploadPath + filePath);
            if (fileSizeKB > maxCompressedImageSizeKB && !"gif".equalsIgnoreCase(imageSuffix)) {
                // 需要压缩图片
                if (width > MAX_IMAGE_WIDTH) {
                    float radio = (float) width / MAX_IMAGE_WIDTH;
                    height = (int) (height / radio);
                }
                Thumbnails.of(bufferedImage)
                        .size(width, height)
                        .toFile(fileUploadPath + filePath + fileName);
            } else {
                FileUtil.writeBytes(bytes, fileUploadPath + filePath + fileName);
            }
            // 保存图片信息
            long newFileSizeB = FileUtil.size(new File(fileUploadPath + filePath + fileName));
            imageApplication.save(ImageCmd.builder()
                    .imagePath(filePath + fileName)
                    .imageType(ImageTypeV.THREAD)
                    .fileSize(newFileSizeB)
                    .fileHash(fileHash)
                    .fileExt(imageSuffix)
                    .storageType(StorageTypeV.LOCAL)
                    .build());
            return filePath + fileName;
        } catch (IOException e) {
            throw new BizException(e.getMessage());
        }
    }

    public String downloadImage(String imageUrl) {
        if (StringUtils.isEmpty(imageUrl) || !imageUrl.startsWith("http")) {
            return "";
        }
        try {
            // 从URL下载图片为字节数组
            byte[] imageData = HttpDownloader.downloadBytes(imageUrl, 10 * 1000);
            return saveImage(imageData, "", 0);
        } catch (Exception e) {
            log.error("下载图片失败，imageUrl = {}", imageUrl);
            return "";
        }
    }

    /**
     * 通过文件头判断（更可靠）
     *
     * @param bytes
     * @return
     */
    private String detectByFileHeader(byte[] bytes) {
        if (bytes.length >= 4) {
            // PNG: 89 50 4E 47
            if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 &&
                    bytes[2] == 0x4E && bytes[3] == 0x47) {
                return "png";
            }
            // JPEG: FF D8 FF
            if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
                return "jpeg";
            }
            // GIF: 47 49 46 38
            if (bytes[0] == 0x47 && bytes[1] == 0x49 &&
                    bytes[2] == 0x46 && bytes[3] == 0x38) {
                return "gif";
            }
        }
        throw new BizException("unknown image type");
    }

    /**
     * 上传用户头像
     *
     * @param file
     * @return
     */
    public Object uploadAvatar(MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new ValidationException("图片文件不能为空！");
        }
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        if (!contentType.startsWith("image")) {
            throw new ValidationException("图片格式不正确！");
        }
        String imageSuffix = CharSequenceUtil.subAfter(contentType, "/", true);
        try {
            // 获取文件的字节
            byte[] bytes = file.getBytes();
            BufferedImage bufferedImage = ImgUtil.toImage(bytes);
            String fileHash = DigestUtil.md5Hex(bytes);
            ImageVO imageVO = imageApplication.getImage(fileHash, ImageTypeV.AVATAR);
            if (imageVO != null) {
                return imageVO.getImagePath();
            }
            String fileName = "/" + fileHash + "." + imageSuffix;
            String filePath = "/uploads/avatar/" + DateUtil.format(DateUtil.date(), "yyyyMM");
            createDirPath(fileUploadPath + filePath);
            Thumbnails.of(bufferedImage)
                    .sourceRegion(Positions.CENTER, 400, 400)
                    .size(200, 200)
                    .keepAspectRatio(false)
                    .toFile(fileUploadPath + filePath + fileName);
            // 保存图片信息
            long fileSizeB = FileUtil.size(new File(fileUploadPath + filePath + fileName));
            imageApplication.save(ImageCmd.builder()
                    .imagePath(filePath + fileName)
                    .imageType(ImageTypeV.AVATAR)
                    .fileSize(fileSizeB)
                    .fileHash(fileHash)
                    .fileExt(imageSuffix)
                    .storageType(StorageTypeV.LOCAL)
                    .build());
            return filePath + fileName;
        } catch (IOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * 创建多级目录
     *
     * @param dirPath
     */
    private void createDirPath(String dirPath) {
        // 使用 FileUtil 判断目录是否存在
        boolean exists = FileUtil.exist(dirPath);
        if (!exists) {
            // 使用 FileUtil 创建多级目录
            FileUtil.mkdir(dirPath);
        }
    }

    /**
     * 删除图片
     *
     * @param imagePath
     * @param storageTypeV
     */
    public void deleteImage(String imagePath, StorageTypeV storageTypeV) {
        if (StringUtils.isEmpty(imagePath)) {
            return;
        }
        try {
            // 删除图片文件
            FileUtil.del(fileUploadPath + imagePath);
        } catch (Exception e) {
            log.error("删除图片失败，imagePath = {}", imagePath);
            throw new BizException("删除图片失败：" + e.getMessage());
        }
    }
}
