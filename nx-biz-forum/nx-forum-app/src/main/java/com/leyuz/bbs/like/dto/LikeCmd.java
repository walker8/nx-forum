package com.leyuz.bbs.like.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeCmd {

    @NotNull
    private Integer targetType;

    @NotNull
    private Long targetId;
}