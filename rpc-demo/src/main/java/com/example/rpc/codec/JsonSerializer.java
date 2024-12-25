package com.example.rpc.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static byte[] serialize(Object obj) throws IOException {
        return objectMapper.writeValueAsBytes(obj);
    }
    
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        return objectMapper.readValue(bytes, clazz);
    }
} 