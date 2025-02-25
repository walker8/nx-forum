package com.leyuz.uc.user.dto;

import com.alibaba.cola.dto.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserQuery extends Query {
    @Serial
    private static final long serialVersionUID = 3372400457990252129L;
    private Long userId;
    private String userName;
    private String lastActiveIp;
    private Integer pageNo;
    private Integer pageSize;
    private String orderBy;
}
