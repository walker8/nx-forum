package com.leyuz.module.cache.jackson;

import com.alicp.jetcache.anno.support.DefaultSpringEncoderParser;

import java.util.function.Function;

public class JacksonEncoderParser extends DefaultSpringEncoderParser {
    public static final String SERIAL_POLICY_JACKSON = "JACKSON";

    public JacksonEncoderParser() {
    }

    @Override
    public Function<Object, byte[]> parseEncoder(String valueEncoder) {
        return (SERIAL_POLICY_JACKSON.equalsIgnoreCase(valueEncoder) ? new JacksonValueEncoder() : super.parseEncoder(valueEncoder));
    }

    @Override
    public Function<byte[], Object> parseDecoder(String valueDecoder) {
        return (SERIAL_POLICY_JACKSON.equalsIgnoreCase(valueDecoder) ? new JacksonValueDecoder() : super.parseDecoder(valueDecoder));
    }
}
