package com.houyu.common.app.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.QuickConfig;
import com.houyu.common.app.config.AppProperties;
import com.houyu.common.app.enums.IdempotentStatus;
import jakarta.annotation.PostConstruct;

public class IdempotentService {

    private Cache<String, String> idempotentCache;

    private final CacheManager cacheManager;
    private final AppProperties appProperties;

    private static final String PROCESSING_PREFIX = "P:";
    private static final String SUCCESS_PREFIX = "S:";

    public IdempotentService(CacheManager cacheManager, AppProperties appProperties) {
        this.cacheManager = cacheManager;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        int successExpire = appProperties.getIdempotent().getSuccessExpireSeconds();

        idempotentCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("app:idempotent")
                        .cacheType(CacheType.REMOTE)
                        .expire(successExpire)
                        .keyConvertor("fastjson2")
                        .build()
        );
    }

    public IdempotentStatus getStatus(String key) {
        String value = idempotentCache.get(key);
        if (value == null) {
            return null;
        }
        if (value.startsWith(PROCESSING_PREFIX)) {
            return IdempotentStatus.PROCESSING;
        }
        if (value.startsWith(SUCCESS_PREFIX)) {
            return IdempotentStatus.SUCCESS;
        }
        return null;
    }

    public boolean tryStoreProcessing(String key) {
        int processingExpire = appProperties.getIdempotent().getProcessingExpireSeconds();
        String processingValue = PROCESSING_PREFIX + System.currentTimeMillis();
        String previous = idempotentCache.putIfAbsent(key, processingValue, processingExpire);
        return previous == null;
    }

    public void store(String key, IdempotentStatus status) {
        if (status == IdempotentStatus.PROCESSING) {
            idempotentCache.put(key, PROCESSING_PREFIX + System.currentTimeMillis());
        } else {
            idempotentCache.put(key, SUCCESS_PREFIX + System.currentTimeMillis());
        }
    }

    public void storeSuccess(String key) {
        idempotentCache.put(key, SUCCESS_PREFIX + System.currentTimeMillis());
    }

    public void remove(String key) {
        idempotentCache.remove(key);
    }
}