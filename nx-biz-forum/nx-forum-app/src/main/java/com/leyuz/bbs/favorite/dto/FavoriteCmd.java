package com.leyuz.bbs.favorite.dto;

import lombok.Data;

@Data
public class FavoriteCmd {
    private Long threadId;
    private Boolean isFavorite;
} 