package com.chirag.news.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.io.IOException;

public class JsonRedisSerDe {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String serialize(Object object) throws JsonProcessingException {
        if(object instanceof String)
            return String.valueOf(object);
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    public static <T> T deserialize(String string, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(string, clazz);
    }

    public static <T>T deserialize(String value, TypeReference<T> typeReference) throws IOException {
        if(!(typeReference.getType() instanceof ParameterizedTypeImpl) && (String.class.isAssignableFrom((Class<T>)typeReference.getType()) || Object.class.isAssignableFrom((Class<T>) typeReference.getType()))){
            return OBJECT_MAPPER.convertValue(value, typeReference);
        }
        else{
            return OBJECT_MAPPER.readValue(value, typeReference);
        }
    }

}
