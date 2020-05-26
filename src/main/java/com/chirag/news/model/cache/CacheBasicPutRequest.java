package com.chirag.news.model.cache;

public class CacheBasicPutRequest<T> extends CachePutRequest {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheBasicPutRequest{");
        sb.append("namespace='").append(getNamespace()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append("isUpdateTtl=").append(isUpdateTtl());
        sb.append(", ttl=").append(getTtl());
        sb.append("value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}

