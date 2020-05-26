package com.chirag.news.model.cache;

import java.util.Map;

public class CacheMapPutRequest<T> extends CachePutRequest {

    private Map<String, T> map;

    public Map<String, T> getMap() {
        return map;
    }

    public void setMap(Map<String, T> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheMapPutRequest{");
        sb.append("namespace='").append(getNamespace()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append("isUpdateTtl=").append(isUpdateTtl());
        sb.append(", ttl=").append(getTtl());
        sb.append("map=").append(map);
        sb.append('}');
        return sb.toString();
    }
}

