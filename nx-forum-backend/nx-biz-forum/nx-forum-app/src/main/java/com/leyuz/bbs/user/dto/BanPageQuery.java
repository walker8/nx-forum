package com.leyuz.bbs.user.dto;

import com.leyuz.common.mybatis.PageQuery;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BanPageQuery {
    private Long userId;
    private Integer forumId;
    private int pageNo;
    private int pageSize;

    public BanPageQuery(Long userId, Integer forumId, int pageNo, int pageSize) {
        this.userId = userId;
        this.forumId = forumId;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public PageQuery toPageQuery() {
        Map<String, Object> params = new HashMap<>();
        if (userId != null) {
            params.put("userId", userId);
        }
        return PageQuery.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .params(params)
                .orderByColumn("create_time")
                .isAsc(false)
                .build();
    }
} 