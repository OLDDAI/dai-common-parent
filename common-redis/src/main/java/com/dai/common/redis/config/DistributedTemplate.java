package com.dai.common.redis.config;

import com.dai.common.redis.constant.LuaConstant;
import com.dai.common.util.tools.IdUtils;
import com.dai.common.util.tools.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 */
@Component
public class DistributedTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedTemplate.class);
    private static final ThreadLocal<Map<String, String>> CURRENT_LOCK = new ThreadLocal<>();
    private static final Random r = new Random();
    /**
     * 默认锁前缀
     */
    private static final String LOCK_PREFIX = "distributedLock:%s";
    /**
     * 默认锁key时间秒
     */
    private static final long initLockTime = 60L;
    /**
     * 默认重试休眠时间65毫秒
     */
    private static final int initRandomRetryTimeout = 65;
    /**
     * 默认获取锁等待时间10秒
     */
    private static final long initTimeout = 10L;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加锁
     *
     * @param key Key
     * @return
     */
    public boolean lock(String key) {
        return this.lock(key, initTimeout, initLockTime, initRandomRetryTimeout);
    }

    /**
     * 加锁
     *
     * @param key     Key
     * @param timeout 等待时间
     * @return
     */
    public boolean lock(String key, long timeout) {
        return this.lock(key, timeout, initLockTime, initRandomRetryTimeout);

    }

    /**
     * 加锁
     *
     * @param key                Key
     * @param timeout            等待时间
     * @param randomRetryTimeout 随机休眠时间
     * @return
     */
    public boolean lock(String key, long timeout, int randomRetryTimeout) {
        return this.lock(key, timeout, initLockTime, randomRetryTimeout);

    }

    /**
     * 加锁
     *
     * @param key                Key
     * @param timeout            等待最大时间
     * @param lockTime           锁key时间
     * @param randomRetryTimeout 随机休眠时间
     * @return
     */
    public boolean lock(String key, long timeout, long lockTime, int randomRetryTimeout) {
        if (lockOnceWithLockTime(key, lockTime)) {
            return true;
        }
        //重试获取锁
        LOGGER.info("retry to acquire lock:" + Thread.currentThread().getName());
        long expMs = System.currentTimeMillis() + timeout * 1000;
        int count = 1;
        while (System.currentTimeMillis() < expMs) {
            try {
                TimeUnit.MILLISECONDS.sleep(15 + r.nextInt(randomRetryTimeout));
                if (lockOnceWithLockTime(key, lockTime)) {
                    return true;
                }
                LOGGER.info(count++ + " times retry to acquire lock for " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                LOGGER.error("acquire redis lock occured an exception:" + Thread.currentThread().getName(), e);
                unlock(key);
            }

        }
        return false;
    }

    /**
     * 获取锁一次，指定锁key时间
     *
     * @param key      Key
     * @param lockTime 锁key时间
     */
    public boolean lockOnceWithLockTime(String key, long lockTime) {
        checkKey(key);
        final String lockKey = getLockKey(key);
        final String value = IdUtils.simpleUUID();
        LOGGER.info("acquiring lock:" + Thread.currentThread().getName() + ",redisKey = " + key);
        //组装lua脚本参数
        List<String> keys = Arrays.asList(lockKey, value, String.valueOf(lockTime));
        Long result = (Long) redisTemplate.execute(packageRedisScript(LuaConstant.lock), keys);
        //获取锁成功
        //获取锁成功
        if (1 == result) {
            putLockValue(lockKey, value);
            LOGGER.info("success to acquire lock:" + Thread.currentThread().getName() + ", Status code reply:" + result);
            return true;
        }
        return false;
    }

    /**
     * 获取锁一次，不尝试
     *
     * @param key Key
     */
    public boolean lockOnce(String key) {
        return this.lockOnceWithLockTime(key, initLockTime);
    }

    private static DefaultRedisScript packageRedisScript(String luaConstant) {
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptText(luaConstant);
        defaultRedisScript.setResultType(Long.class);
        return defaultRedisScript;
    }

    /**
     * 释放KEY
     *
     * @param key
     * @return
     */
    public boolean unlock(String key) {
        final String lockKey = getLockKey(key);
        checkKey(key);
        String value = CURRENT_LOCK.get().get(lockKey);
        if (StringUtils.isEmpty(value)) {
            LOGGER.error("release lock occured an error: lock key not found;{}", key);
            return false;
        }
        //组装lua脚本参数
        List<String> keys = Arrays.asList(lockKey, value);
        LOGGER.info("unlock :::: redisKey = " + key + " value = " + value);
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript();
        defaultRedisScript.setScriptText(LuaConstant.unLock);
        defaultRedisScript.setResultType(Long.class);
        Long result = (Long) redisTemplate.execute(defaultRedisScript, keys);
        //如果这里抛异常，后续锁无法释放
        clean(lockKey);
        if (result == 1) {
            LOGGER.info("release lock success:" + Thread.currentThread().getName() + ", Status code reply=" + result);
            return true;
        } else if (result == -1) {
            LOGGER.warn("release lock exception:" + Thread.currentThread().getName() + ", key has expired or released. Status code reply=" + result);
        } else {
            //其他情况，一般是删除KEY失败，返回0
            LOGGER.error("release lock failed:" + Thread.currentThread().getName() + ", del key failed. Status code reply=" + result);
        }
        return false;
    }

    /**
     * 清除本地线程变量，防止内存泄露
     */
    private static void clean(String key) {
        if (CURRENT_LOCK.get() == null) {
            return;
        }
        CURRENT_LOCK.get().remove(key);
        if (CURRENT_LOCK.get().size() == 0) {
            CURRENT_LOCK.remove();
        }
    }

    /**
     * 添加锁值
     */
    private static void putLockValue(String key, String value) {
        if (CURRENT_LOCK.get() == null) {
            CURRENT_LOCK.set(new HashMap<>(1));
        }
        CURRENT_LOCK.get().put(key, value);
    }

    /**
     * 清除本地线程变量，防止内存泄露
     */
    private static void checkKey(String key) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("try to acquire lock,but the key is not passed in");
        }
    }

    /**
     * 获取lock  RedisKey
     *
     * @param key 原始KEY
     * @return
     */
    public static String getLockKey(String key) {
        return String.format(LOCK_PREFIX, key);
    }

    /**
     * 判断lock是否存在
     *
     * @param key 原始KEY
     * @return
     */
    public boolean existLockKey(String key) {
        return redisTemplate.hasKey(getLockKey(key));
    }
}
