package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
public class UserVO extends DTO {
    @Serial
    private static final long serialVersionUID = -1513825297767877964L;
    private String userName;
    private String avatar;
    private Long userId;
    private String intro;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastActiveDate;
}
