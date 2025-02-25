package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import lombok.Data;

import java.io.Serial;

@Data
public class UserResp extends DTO {
    @Serial
    private static final long serialVersionUID = -1636278773987615631L;
    private String accessToken;
    private String tokenType;
    private Long userId;
    private String token;
    private Long tokenExpiresIn;
}
