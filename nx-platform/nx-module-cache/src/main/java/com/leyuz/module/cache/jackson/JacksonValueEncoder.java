package com.leyuz.module.cache.jackson;

import com.alicp.jetcache.support.AbstractValueEncoder;
import com.alicp.jetcache.support.CacheEncodeException;

public class JacksonValueEncoder extends AbstractValueEncoder {
    public static final JacksonValueEncoder INSTANCE = new JacksonValueEncoder();

    public JacksonValueEncoder() {
        super(false);
    }

    @Override
    public byte[] apply(Object value) {
        try {
            return JACKSON.MAPPER.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new CacheEncodeException("Json Encode error. " + "msg=" + e.getMessage(), e);
        }
    }
}
