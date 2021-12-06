package com.dai.common.constant.response;

import com.dai.common.constant.enums.CodeDefine;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:19
 */
public enum GlobalResponseResultEnum implements CodeDefine<Integer> {
    /**
     * 请求状态
     */
    ERROR(0, "请求失败"),
    SUCCESS(1, "请求成功"),
    ;
    private Integer code;
    private String desc;

    GlobalResponseResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return desc;
    }
}
