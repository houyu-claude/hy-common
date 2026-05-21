package com.houyu.common.app.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.support.QuickConfig;
import com.houyu.common.app.config.AppProperties;
import com.houyu.common.app.enums.IdempotentStatus;
import jakarta.annotation.PostConstruct;

public class IdempotentService {

    private Cache<String, IdempotentStatus> processingCache;
    private Cache<String, IdempotentStatus> successCache;

    private final CacheManager cacheManager;
    private final AppProperties appProperties;

    public IdempotentService(CacheManager cacheManager, AppProperties appProperties) {
        this.cacheManager = cacheManager;
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void init() {
        int processingExpire = appProperties.getIdempotent().getProcessingExpireSeconds();
        int successExpire = appProperties.getIdempotent().getSuccessExpireSeconds();

        processingCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("app:idempotent:processing")
                        .cacheType(CacheType.REMOTE)
                        .expire(processingExpire)
                        .keyConvertor("fastjson2")
                        .build()
        );

        successCache = cacheManager.getOrCreateCache(
                QuickConfig.newBuilder("app:idempotent:success")
                        .cacheType(CacheType.REMOTE)
                        .expire(successExpire)
                        .keyConvertor("fastjson2")
                        .build()
        );
    }

    public IdempotentStatus getStatus(String key) {
        IdempotentStatus processing = processingCache.get(key);
        if (processing != null) {
            return processing;
        }
        return successCache.get(key);
    }

    public boolean tryStoreProcessing(String key) {
        IdempotentStatus previous = processingCache.putIfAbsent(key, IdempotentStatus.PROCESSING);
        return previous == null;
    }

    public void store(String key, IdempotentStatus status) {
        if (status == IdempotentStatus.PROCESSING) {
            processingCache.put(key, status);
        } else {
            successCache.put(key, status);
        }
    }

    public void storeSuccess(String key) {
        processingCache.remove(key);
        successCache.put(key, IdempotentStatus.SUCCESS);
    }

    public void remove(String key) {
        processingCache.remove(key);
        successCache.remove(key);
    }
}