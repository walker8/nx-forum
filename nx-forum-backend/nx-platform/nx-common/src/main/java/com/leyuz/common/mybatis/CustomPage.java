package com.leyuz.common.mybatis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public <R> CustomPage<R> map(Function<? super T, ? extends R> converter) {
        List<R> convertedRecords = records.stream().map(converter).collect(Collectors.toList());
        return new CustomPage<>(convertedRecords, total, size, current, hasNext);
    }
}
