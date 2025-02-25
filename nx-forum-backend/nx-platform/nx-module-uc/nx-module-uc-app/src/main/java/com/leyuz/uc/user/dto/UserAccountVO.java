package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.DTO;
import lombok.Data;

import java.io.Serial;

@Data
public class UserAccountVO extends DTO {
    @Serial
    private static final long serialVersionUID = -1513825297767877964L;
    private String email;
    private String password;
    private String phone;
}
