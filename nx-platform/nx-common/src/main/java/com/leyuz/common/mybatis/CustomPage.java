package com.leyuz.common.mybatis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomPage<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private boolean hasNext;
}
