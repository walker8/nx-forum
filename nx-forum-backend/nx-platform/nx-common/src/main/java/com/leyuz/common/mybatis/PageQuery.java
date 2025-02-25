package com.leyuz.common.mybatis;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 当前页数
     */
    private Integer pageNo;
    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 排序列
     */
    @Getter
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    @Getter
    private Boolean isAsc;

    /**
     * 查询参数
     */
    private transient Map<String, Object> params = new HashMap<>();

    /**
     * 当前记录起始索引 默认值
     */
    public static final int DEFAULT_PAGE_NO = 1;

    /**
     * 每页显示记录数 默认值 默认查全部
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>(16);
        }
        return params;
    }

    public Integer getPageSize() {
        if (pageSize == null || pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public Integer getPageNo() {
        if (pageNo == null || pageNo <= 0) {
            pageNo = DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    public <T> Page<T> toPage() {
        return Page.of(getPageNo(), getPageSize());
    }

    /**
     * 查询条件
     *
     * @param <T>
     * @return
     */
    public <T> QueryWrapper<T> toQueryWrapper() {
        return toQueryWrapper("");
    }

    public <T> QueryWrapper<T> toQueryWrapper(String tableAlias) {
        QueryWrapper<T> queryWrapper = toQueryAllWrapper(tableAlias);
        Boolean isDeleted = MapUtil.getBool(params, "isDeleted");
        // 默认查未删除的数据
        queryWrapper.eq(tableAlias + "is_deleted", Boolean.TRUE.equals(isDeleted));
        return queryWrapper;
    }

    /**
     * 查询条件（全部）
     *
     * @param <T>
     * @return
     */
    public <T> QueryWrapper<T> toQueryAllWrapper(String tableAlias) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(orderByColumn)) {
            if (Boolean.TRUE.equals(isAsc)) {
                queryWrapper.orderByAsc(tableAlias + orderByColumn);
            } else {
                queryWrapper.orderByDesc(tableAlias + orderByColumn);
            }
        }
        return queryWrapper;
    }

}
