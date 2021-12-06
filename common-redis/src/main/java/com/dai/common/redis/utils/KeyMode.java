package com.dai.common.redis.utils;


/**
 * Key生成模式
 */
public enum KeyMode {

    /**
     * 固定不变的Key
     */
    Fixed,


    /**
     * 带有参数的Key
     */
    Params,

}
