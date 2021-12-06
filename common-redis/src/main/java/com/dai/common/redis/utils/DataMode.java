package com.dai.common.redis.utils;


/**
 * Redis现有支持的数据类型枚举
 */
public enum DataMode {
    Incr,
    String,
    Hash,
    List,
    Set,
    Sortedset,
    Pubsub
}
