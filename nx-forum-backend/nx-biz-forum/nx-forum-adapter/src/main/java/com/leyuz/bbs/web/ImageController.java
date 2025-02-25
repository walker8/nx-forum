package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.system.attach.AttachApplication;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "图片管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class ImageController {

    private final AttachApplication attachApplication;
    private final ForumPermissionResolver forumPermissionResolver;

    @PostMapping("/upload")
    public SingleResponse upload(@RequestParam("file") MultipartFile file) {
        forumPermissionResolver.checkPermission("image:upload");
        return SingleResponse.of(attachApplication.uploadImage(file));
    }

    @PostMapping("/avatar/upload")
    public SingleResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        forumPermissionResolver.checkPermission("avatar:upload");
        return SingleResponse.of(attachApplication.uploadAvatar(file));
    }
}
