package com.dai.common.util.tools;

import java.util.UUID;

/**
 * ID生成器工具类
 */
public class IdUtils {
    /**
     * 获取随机UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 去除横线
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
