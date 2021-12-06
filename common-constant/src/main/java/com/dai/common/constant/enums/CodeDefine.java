package com.dai.common.constant.enums;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 14:50
 */
public interface CodeDefine<T> {
    /**
     * 获取code
     * @return
     */
    T getCode();

    /**
     * 获取msg
     * @return
     */
    String getMsg();
}
