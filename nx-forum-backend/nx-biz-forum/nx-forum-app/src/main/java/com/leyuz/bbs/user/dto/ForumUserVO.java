package com.leyuz.bbs.user.dto;

import com.alibaba.cola.dto.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

@Data
@Builder
public class ForumUserVO extends DTO {
    @Serial
    private static final long serialVersionUID = 8738489184354527615L;
    private String userName;
    private String avatar;
    private Long userId;
    private String intro;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime lastActiveDate;
    private Boolean followed;
}
