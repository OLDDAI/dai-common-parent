package com.dai.common.redis.utils;

/**
 * Redis 过期时间累加方式
 */
public enum TTLMode {

    /**
     * 永久，就是不设置过期时间
     */
    NONE,
    /**
     * 当前时间增加指定的秒数
     * 比如当前时间是2018-6-4 22:00:00 ，增加 3 * 3600 秒
     * 那么失效时间在：2018-6-5 01:00:00
     */
    NOW_ADD,
    /**
     * 动态过期时间，由开发人员控制，通常会存在多个过期时间，
     */
    DYNAMIC,




}
