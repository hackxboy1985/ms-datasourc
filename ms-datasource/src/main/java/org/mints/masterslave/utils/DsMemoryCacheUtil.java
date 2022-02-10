package org.mints.masterslave.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.mints.masterslave.logger.MsLogger;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DsMemoryCacheUtil<T> {
    private static final MsLogger logger = MsLogger.getLogger(DsMemoryCacheUtil.class);

    @Value("${ms-datasource.cacheTimeMinutes:10}")
    Integer memcacheTimeMinutes;

    Cache<String,T> cache;


    @PostConstruct
    void init(){
        cache = CacheBuilder.newBuilder().expireAfterAccess(memcacheTimeMinutes, TimeUnit.MINUTES).build();
    }

    /**
     * 支持高并发的内存缓存，失败则调用callable获取
     * @param key 缓存的key
     * @param callable 获取原始数据的方法
     * @return
     */
    public Optional<T> findObject(String key, Callable<T> callable) {
        try {
            T t = cache.get(key, callable);
            return Optional.ofNullable(t);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return Optional.ofNullable(null);
    }

}
