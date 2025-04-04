package com.leyuz.module.cache.jackson;

import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.support.AbstractValueDecoder;

import java.io.IOException;

public class JacksonValueDecoder extends AbstractValueDecoder {
    public static final JacksonValueEncoder INSTANCE = new JacksonValueEncoder();

    public JacksonValueDecoder() {
        super(false);
    }

    @Override
    public Object doApply(byte[] buffer) throws IOException {
        return JACKSON.MAPPER.readValue(buffer, CacheValueHolder.class);
    }
}
