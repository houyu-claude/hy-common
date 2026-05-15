package com.houyu.common.log.trace;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnBean(RedissonClient.class)
public class MachineIdManager {

    private static final String MACHINE_LOCK_PREFIX = "log:trace:{machine_lock}:";
    private static final int MAX_MACHINE_ID = 9999;
    private static final long LOCK_WAIT_SECONDS = 0;

    @CreateCache(name = "log:trace:allocated_machines",
                 cacheType = CacheType.REMOTE,
                 expire = 300,
                 timeUnit = TimeUnit.SECONDS)
    private Cache<Integer, String> allocatedMachines;

    private final RedissonClient redissonClient;
    private String instanceId;
    private volatile String machineId;
    private volatile RLock heldLock;

    public MachineIdManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @PostConstruct
    public void init() {
        instanceId = UUID.randomUUID().toString();
        machineId = registerMachineId();
    }

    private String registerMachineId() {
        for (int i = 0; i <= MAX_MACHINE_ID; i++) {
            String lockKey = MACHINE_LOCK_PREFIX + i;
            RLock lock = redissonClient.getLock(lockKey);
            boolean acquired = false;
            try {
                acquired = lock.tryLock(LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
                if (acquired) {
                    String owner = allocatedMachines.get(i);
                    if (owner == null) {
                        allocatedMachines.put(i, instanceId);
                        heldLock = lock;
                        return String.format("%04d", i);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (acquired && heldLock != lock) {
                    try { lock.unlock(); } catch (Exception ignored) {}
                }
            }
        }
        return getFallbackMachineId();
    }

    private String getFallbackMachineId() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            int hash = Math.abs(ip.hashCode()) % 10000;
            return String.format("%04d", hash);
        } catch (Exception e) {
            return String.format("%04d", (int) (Math.random() * 10000));
        }
    }

    @PreDestroy
    public void destroy() {
        if (heldLock != null) {
            try {
                if (heldLock.isHeldByCurrentThread()) {
                    heldLock.unlock();
                }
            } catch (Exception ignored) {}
        }
        if (machineId != null) {
            try {
                allocatedMachines.remove(Integer.parseInt(machineId));
            } catch (Exception ignored) {}
        }
    }

    public String getMachineId() {
        return machineId != null ? machineId : "0000";
    }
}