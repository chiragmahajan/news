package com.chirag.news.service.impl;

import com.chirag.news.config.RedisConfig;
import com.chirag.news.exception.KeyNotFoundException;
import com.chirag.news.model.cache.CacheBasicPutRequest;
import com.chirag.news.model.cache.CacheListPutRequest;
import com.chirag.news.model.cache.CacheMapPutRequest;
import com.chirag.news.model.cache.CacheRequest;
import com.chirag.news.model.cache.CacheSetPutRequest;
import com.chirag.news.service.CacheService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("newsRedisCache")
public class RedisCacheServiceImpl implements CacheService {

    private static final Logger LOGGER = LogManager.getLogger(RedisCacheServiceImpl.class);

    private static final String SEPARATOR = "::";

    private static final String MASTER_NAME = "cluster";

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Autowired
    private RedisConfig redisConfig;

    private Pool<Jedis> pool;


    @PostConstruct
    public void init() {
        try {
            LOGGER.info("Initializing redis with settings: " + redisConfig);
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(redisConfig.getPoolSize());
            if (redisConfig.getSentinelOn() != null && redisConfig.getSentinelOn()) {
                pool = new JedisSentinelPool(MASTER_NAME, redisConfig.getSentinels(), jedisPoolConfig);
            } else if (redisConfig.getPort() != null) {
                pool = new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(), redisConfig.getConnectionTimeout());
            } else {
                pool = new JedisPool(jedisPoolConfig, redisConfig.getHost());
            }

            LOGGER.info("Redis connection established");
        } catch (Exception e) {
            LOGGER.error("Error in creating jedis pool {}", e.fillInStackTrace());
        }

    }


    @Override
    public <T> void put(CacheBasicPutRequest<T> cacheBasicPutRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for put : %s", cacheBasicPutRequest));
            }
            String key = cacheBasicPutRequest.getNamespace() + SEPARATOR + cacheBasicPutRequest.getKey();
            if (cacheBasicPutRequest.isUpdateTtl() == false) {
                if (cacheBasicPutRequest.getTtl() > 0) {
                    long remainingTime = jedis.ttl(key);
                    if (remainingTime > 0) {
                        jedis.setex(key, (int) remainingTime, JsonRedisSerDe.serialize(cacheBasicPutRequest.getValue()));
                    } else {
                        jedis.setex(key, cacheBasicPutRequest.getTtl(), JsonRedisSerDe.serialize(cacheBasicPutRequest.getValue()));
                    }
                } else {
                    jedis.set(key, JsonRedisSerDe.serialize(cacheBasicPutRequest.getValue()));
                }

            } else {
                jedis.setex(key, cacheBasicPutRequest.getTtl(), JsonRedisSerDe.serialize(cacheBasicPutRequest.getValue()));
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request buildQuery completed for put "));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> void incrementKeyValue(CacheBasicPutRequest<T> cacheBasicPutRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for put : %s", cacheBasicPutRequest));
            }
            String key = cacheBasicPutRequest.getNamespace() + SEPARATOR + cacheBasicPutRequest.getKey();

            jedis.incrBy(key, (Integer) cacheBasicPutRequest.getValue());
            if (cacheBasicPutRequest.isUpdateTtl()) {
                jedis.expire(key, cacheBasicPutRequest.getTtl());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request buildQuery completed for put "));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> T get(CacheRequest cacheRequest, TypeReference<T> typeReference) throws KeyNotFoundException, IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for get : %s", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();

            String value = jedis.get(key);
            if (value == null) throw new KeyNotFoundException("Key does not exist");
            T obj = mapper.readValue(value, typeReference);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for get : %s", obj));
            }
            return obj;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }


    }

    @Override
    public <T> List<T> getMultipleKeysSameType(TypeReference<T> typeReference, String... keys) {
        Jedis jedis = null;
        List<T> valuesObj = new ArrayList<>();
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getMultipleKeysSameType : %s", Arrays.toString(keys)));
            }
            List<String> valuesStr = jedis.mget(keys);
            if (valuesStr != null) {
                for (String entry : valuesStr) {
                    if (entry != null) {
                        valuesObj.add(JsonRedisSerDe.deserialize(entry, typeReference));
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("IO exception", e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Response for getMultipleKeysSameType : %s", valuesObj));
        }
        return valuesObj;
    }


    @Override
    public void trimList(CacheRequest cacheRequest, int start, int end) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for trimList : %s , page start : %s and end :%s ", cacheRequest, start, end));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            if (jedis.exists(key)) {
                jedis.ltrim(key, start, end);

            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request buildQuery completed for trimList "));
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }


    }

    @Override
    public <T> void putList(CacheListPutRequest<T> cacheListPutRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for putList : %s", cacheListPutRequest));
            }
            String key = cacheListPutRequest.getNamespace() + SEPARATOR + cacheListPutRequest.getKey();
            for (T value : cacheListPutRequest.getValues()) {
                jedis.lpush(key, JsonRedisSerDe.serialize(value));
            }
            if (cacheListPutRequest.isUpdateTtl()) {
                jedis.expire(key, cacheListPutRequest.getTtl());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request buildQuery complete for putList"));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public <T> List<T> getList(CacheRequest cacheRequest, Integer start, Integer end, TypeReference<T> typeReference) throws KeyNotFoundException, IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getList : %s,Page start: %s and end : %s", cacheRequest, start, end));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();

            List<String> strList = jedis.lrange(key, start, end);
            if (strList == null || strList.isEmpty()) throw new KeyNotFoundException("Key does not exist");
            List<T> objList = new ArrayList<>();

            for (String aList : strList) {
               // T object = JsonRedisSerDe.deserialize(aList, typeReference);
                T object = mapper.readValue(aList, typeReference);
                objList.add(object);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getList : %s", objList));
            }

            return objList;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T extends Serializable> List<T> getList(CacheRequest cacheRequest, Integer start, Integer end, Class<T> clazz)
            throws KeyNotFoundException, IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getList : %s,Page start: %s and end : %s", cacheRequest, start, end));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();

            List<String> strList = jedis.lrange(key, start, end);
            if (strList == null || strList.isEmpty()) throw new KeyNotFoundException("Key does not exist");
            List<T> objList = new ArrayList<>();
            for (String objectAsString : strList) {
                objList.add(JsonRedisSerDe.deserialize(objectAsString, clazz));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getList : %s", objList));
            }
            return objList;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> T getListItem(CacheRequest cacheRequest, long index, TypeReference<T> typeReference) throws KeyNotFoundException, IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getListItem : %s and inde :%s", cacheRequest, index));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            String strObject = jedis.lindex(key, index);
            if (strObject == null) throw new KeyNotFoundException("Key does not exist");
            T obj = JsonRedisSerDe.deserialize(strObject, typeReference);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getListItem : %s ", obj));
            }
            return obj;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }


    }

    //insert a hashmap<string,object> in redis by serialising object to string
    @Override
    public <T> void putMap(CacheMapPutRequest<T> cacheMapPutRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for putMap : %s ", cacheMapPutRequest));
            }
            String key = cacheMapPutRequest.getNamespace() + SEPARATOR + cacheMapPutRequest.getKey();
            Map<String, String> strMap = new HashMap<String, String>();

            for (Map.Entry<String, T> entry : cacheMapPutRequest.getMap().entrySet()) {
                strMap.put(entry.getKey(), JsonRedisSerDe.serialize(entry.getValue()));
            }
            jedis.hmset(key, strMap);
            if (cacheMapPutRequest.isUpdateTtl()) {
                jedis.expire(key, cacheMapPutRequest.getTtl());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request completed fro putMap "));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }


    @Override
    public <T> Map<String, T> getMap(CacheRequest cacheRequest, TypeReference<T> typeReference) throws KeyNotFoundException, IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getMap : %s ", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            Map<String, String> strMap = jedis.hgetAll(key);
            if (strMap == null || strMap.isEmpty()) throw new KeyNotFoundException("Key does not exist");
            Map<String, T> objMap = new HashMap<>();

            for (Map.Entry<String, String> entry : strMap.entrySet()) {
                objMap.put(entry.getKey(), JsonRedisSerDe.deserialize(entry.getValue(), typeReference));
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getMap : %s ", objMap));
            }
            return objMap;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> T getMapItem(CacheRequest cacheRequest, String field, TypeReference<T> typeReference) throws IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getMapItem : %s and map key :%s", cacheRequest, field));
            }
            T obj = null;
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();

            String str = jedis.hget(key, field);
            if (str != null) {
                obj = JsonRedisSerDe.deserialize(str, typeReference);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getMapItem : %s ", obj));
            }

            return obj;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> void setMapItem(CacheRequest cacheRequest, String field, T value, int ttl) throws IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for setMapItem : %s and map key :%s", cacheRequest, field));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            jedis.hset(key, field, JsonRedisSerDe.serialize(value));
            long existsttl = jedis.ttl(key);
            if (existsttl <= 0) {
                jedis.expire(key, ttl);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request completed for setMapItem "));
            }


        } catch (Exception e) {
            LOGGER.error("error while putting in redis", e);
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public void setKeyExpiry(CacheRequest cacheRequest, int ttlsecond) throws KeyNotFoundException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for setKeyExpiry: %s ", key));
            }

            long response = jedis.expire(key, ttlsecond);
            if (response == 0) {
                throw new KeyNotFoundException("key does not exist");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request completed for setKeyExpiry with response %s ", response));
            }


        } catch (Exception e) {
            LOGGER.error("error while putting in redis", e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public Set<String> getMapKeys(CacheRequest cacheRequest) throws KeyNotFoundException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getMapKeys : %s ", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            Set<String> setStr = jedis.hkeys(key);
            if (setStr == null || setStr.isEmpty()) throw new KeyNotFoundException("Key does not exist");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response] for getMapKeys : %s ", setStr));
            }

            return setStr;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }


    @Override
    public void putSet(CacheSetPutRequest cacheSetPutRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for putSet : %s ", cacheSetPutRequest));
            }
            String key = cacheSetPutRequest.getNamespace() + SEPARATOR + cacheSetPutRequest.getKey();

            for (String str : cacheSetPutRequest.getValues()) {
                jedis.sadd(key, str);
            }

            if (cacheSetPutRequest.isUpdateTtl()) jedis.expire(key, cacheSetPutRequest.getTtl());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request processed for  putSet "));
            }


        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public Set<String> getSet(CacheRequest cacheRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getSet : %s ", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            Set<String> response = jedis.smembers(key);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getSet : %s ", response));
            }
            return response;
        } finally {
            if (jedis != null)
                jedis.close();
        }


    }

    @Override
    public void incrementMapItem(CacheRequest cacheRequest, String field, long value) throws IOException {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for incrementMapItem : %s ,key :%s value: %s", cacheRequest, field, value));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            jedis.hincrBy(key, field, value);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request procees successfully for  incrementMapItem "));
            }
        } catch (Exception e) {
            LOGGER.error("error in serialization", e);
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public Boolean isPresentSet(CacheRequest cacheRequest, String member) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for isPresent : %s ", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            return jedis.sismember(key, member);

        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> void putSortedSet(CacheRequest cacheRequest, T member) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for putSortedSet : %s ", cacheRequest));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            String value = JsonRedisSerDe.serialize(member);
            Double score = jedis.zscore(key, value);
            int updateScore = 1;
            if (score != null)
                updateScore = (int) (score + 1);
            jedis.zadd(key, updateScore, value);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("data push successfully through putSortedSet");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }

    }

    @Override
    public <T> List<T> getSortedSet(CacheRequest cacheRequest, long start, long end, TypeReference<T> typeReference) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Request for getSortedSet : %s ,start :%s , end %s", cacheRequest, start, end));
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            Set<String> set = jedis.zrevrange(key, start - 1, end);
            List<T> list = new ArrayList<>();
            if (set != null) {
                for (String str : set) {
                    list.add(JsonRedisSerDe.deserialize(str, typeReference));
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Response for getSortedSet : %s ", list));
            }

            return list;
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public void deleteKey(CacheRequest cacheRequest) throws Exception {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request for del {}", cacheRequest);
            }
            String key = cacheRequest.getNamespace() + SEPARATOR + cacheRequest.getKey();
            jedis.del(key);
        } catch (Exception e) {
            throw e;
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    @Override
    public void shutdown() {
        if (pool != null) {
            pool.destroy();
        }
    }
}

