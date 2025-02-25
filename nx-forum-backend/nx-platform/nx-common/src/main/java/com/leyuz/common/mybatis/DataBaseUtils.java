package com.leyuz.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

/**
 * 数据库工具类
 */
public class DataBaseUtils {
    /**
     * 将mybatis-plus分页结果转换为自定义分页结果
     *
     * @param page
     * @param converter
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> CustomPage<T> createCustomPage(Page<R> page, Function<R, T> converter) {
        return CustomPage.<T>builder()
                .current(page.getCurrent())
                .hasNext(page.hasNext())
                .records(Optional.of(page.getRecords()).orElse(new ArrayList<>())
                        .stream().map(converter).toList())
                .size(page.getSize())
                .total(page.getTotal())
                .build();
    }
}
