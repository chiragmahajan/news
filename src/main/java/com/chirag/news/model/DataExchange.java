package com.chirag.news.model;

import com.chirag.news.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties (ignoreUnknown = true)
public class DataExchange implements Serializable {

    private Map<String, Object> meta = new HashMap<>();
    private Map<String, Object> data = new HashMap<>();

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void putMeta(String key, Object value) {
        this.meta.put(key, value);
    }

    public Object getMeta(String key) {
        return this.meta.get(key);
    }

    public void putData(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }

    public DataExchange success(String key, Object data) {
        this.putMeta(Constants.DATA_EXCHANGE_META_STATUS_KEY, Constants.DATA_EXCHANGE_META_SUCCESS_KEY);
        if (key != null) {
            this.putData(key, data);
        }
        return this;
    }

    public DataExchange success() {
        return this.success(null, null);
    }

    public DataExchange failure(String message) {
        this.putMeta(Constants.DATA_EXCHANGE_META_STATUS_KEY, Constants.DATA_EXCHANGE_META_FAILED_KEY);
        this.putMeta(Constants.DATA_EXCHANGE_META_FAILURE_REASON_KEY, message);
        return this;
    }
}

