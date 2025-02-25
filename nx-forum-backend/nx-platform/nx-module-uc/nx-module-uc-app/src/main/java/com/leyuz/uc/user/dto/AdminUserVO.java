package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
public class AdminUserVO extends DTO {
    @Serial
    private static final long serialVersionUID = 3837297780341530448L;

    private String userName;
    private String avatar;
    private Long userId;

    private String email;
    private String lastActiveIp;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime lastActiveDate;

    // 帐号状态（0正常 1停用 2已注销）
    private Integer accountStatus;

    private String intro;
}
