package com.chirag.news.model.cache;

public class CachePutRequest extends CacheRequest {

    private boolean isUpdateTtl;
    private int ttl=10;

    public boolean isUpdateTtl() {
        return isUpdateTtl;
    }

    public void setUpdateTtl(boolean updateTtl) {
        isUpdateTtl = updateTtl;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CachePutRequest{");
        sb.append("namespace='").append(getNamespace()).append('\'');
        sb.append(", key='").append(getKey()).append('\'');
        sb.append("isUpdateTtl=").append(isUpdateTtl);
        sb.append(", ttl=").append(ttl);
        return sb.toString();
    }
}

