package com.dai.common.constant.exception;

import com.dai.common.constant.enums.CodeDefine;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 14:54
 */
public class BusiException extends RuntimeException {
    private String code;
    private String msg;

    public BusiException(CodeDefine<String> codeDefine) {
        super(codeDefine.getMsg());
        this.code = codeDefine.getCode();
        this.msg = codeDefine.getMsg();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
