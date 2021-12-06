package com.dai.common.constant.enums;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 14:52
 */
public enum ActiveProfileEnum implements CodeDefine<Integer>{
    /**
     * 开发环境
     */
    DEV(0, "dev"),
    /**
     * 测试环境
     */
    TEST(1, "test"),
    /**
     * 生产环境
     */
    PROD(2, "prod"),
    ;

    private Integer code;

    private String msg;

    ActiveProfileEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
