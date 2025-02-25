package com.leyuz.module.cache.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * jetcache原来支持fastjson序列化器，后因为某些原因去掉了。
 * 但是将缓存已JSON格式存储，方便阅读以及跨平台获取的需求仍然存在。
 * 所以需要开发一个基于jackson的序列化器，实现这个需求。
 */
public class JACKSON {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public JACKSON() {
    }

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
    }
}
