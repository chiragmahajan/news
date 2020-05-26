package com.chirag.news.model.cache;

import java.util.Set;

public class CacheSetPutRequest extends CachePutRequest {
    private Set<String> values;

    public Set<String> getValues() {
        return values;
    }

    public void setValues(Set<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheSetPutRequest{");
        sb.append("namespace='").append(getNamespace()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append("isUpdateTtl=").append(isUpdateTtl());
        sb.append(", ttl=").append(getTtl());
        sb.append("values=").append(values);
        sb.append('}');
        return sb.toString();
    }
}
