package com.chirag.news.model.cache;

import java.util.List;

public class CacheListPutRequest<T> extends CachePutRequest {

    private int index;
    private List<T> values;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CacheListPutRequest{");
        sb.append("namespace='").append(getNamespace()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append("isUpdateTtl=").append(isUpdateTtl());
        sb.append(", ttl=").append(getTtl());
        sb.append("index=").append(index);
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }
}

