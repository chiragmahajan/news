package com.chirag.news.service;

import com.chirag.news.exception.KeyNotFoundException;
import com.chirag.news.model.cache.CacheBasicPutRequest;
import com.chirag.news.model.cache.CacheListPutRequest;
import com.chirag.news.model.cache.CacheMapPutRequest;
import com.chirag.news.model.cache.CacheRequest;
import com.chirag.news.model.cache.CacheSetPutRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CacheService {

    <T> void put(CacheBasicPutRequest<T> cacheBasicPutRequest) throws Exception;

    <T> T get(CacheRequest cacheRequest, TypeReference<T> typeReference) throws KeyNotFoundException, IOException;

    <T> void incrementKeyValue(CacheBasicPutRequest<T> cacheBasicPutRequest) throws Exception;

    <T extends Serializable> List<T> getList(CacheRequest cacheRequest, Integer start, Integer end, Class<T> clazz)
            throws KeyNotFoundException, IOException;

    <T> List<T> getList(CacheRequest cacheRequest, Integer start, Integer end, TypeReference<T> typeReference) throws KeyNotFoundException, IOException;

    <T> T getListItem(CacheRequest cacheRequest, long index, TypeReference<T> typeReference) throws KeyNotFoundException, IOException;

    <T> void putList(CacheListPutRequest<T> cacheListPutRequest) throws Exception;

    void trimList(CacheRequest cacheRequest, int start, int end) throws Exception;

    <T> void putMap(CacheMapPutRequest<T> cacheMapPutRequest) throws Exception;

    <T> Map<String, T> getMap(CacheRequest cacheRequest, TypeReference<T> typeReference) throws KeyNotFoundException, IOException;

    <T> T getMapItem(CacheRequest cacheRequest, String field, TypeReference<T> typeReference) throws KeyNotFoundException, IOException;

    <T> void setMapItem(CacheRequest cacheRequest, String field, T value, int ttl) throws KeyNotFoundException, IOException;

    void setKeyExpiry(CacheRequest cacheRequest, int ttlsecond) throws KeyNotFoundException;

    Set<String> getMapKeys(CacheRequest cacheRequest) throws KeyNotFoundException, IOException;

    void putSet(CacheSetPutRequest cacheSetPutRequest) throws Exception;

    Set<String> getSet(CacheRequest cacheRequest) throws Exception;

    public <T> List<T> getMultipleKeysSameType(TypeReference<T> typeReference, String... keys);

    public void incrementMapItem(CacheRequest cacheRequest, String field, long value) throws KeyNotFoundException, IOException;

    Boolean isPresentSet(CacheRequest cacheRequest, String member) throws Exception;

    <T> void putSortedSet(CacheRequest cacheRequest, T member) throws Exception;

    <T> List<T> getSortedSet(CacheRequest cacheRequest, long start, long end, TypeReference<T> typeReference) throws Exception;

    void deleteKey(CacheRequest cacheRequest) throws Exception;

    void shutdown();
}


