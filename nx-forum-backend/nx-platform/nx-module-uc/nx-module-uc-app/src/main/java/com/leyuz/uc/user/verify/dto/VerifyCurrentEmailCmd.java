package com.leyuz.uc.user.verify.dto;

import java.io.Serial;

import com.alibaba.cola.dto.Command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "验证当前邮箱")
@Data
@EqualsAndHashCode(callSuper = false)
public class VerifyCurrentEmailCmd extends Command {

    @Serial
    private static final long serialVersionUID = -1978630342991538221L;

    @Schema(description = "当前邮箱验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "\\d{6}", message = "验证码格式不正确")
    private String verifyCode;
}
