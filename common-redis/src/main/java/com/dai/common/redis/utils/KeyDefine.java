package com.dai.common.redis.utils;


public interface KeyDefine {

    KeyMode getKeyMode();

    String getKeyPrefix();

    TTLMode getTTLMode();

    Long getSecondValue();

    DataMode getDataType();

    String getTTLDateStr();

}
