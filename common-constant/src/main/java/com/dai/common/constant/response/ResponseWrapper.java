package com.dai.common.constant.response;

import com.dai.common.constant.enums.CodeDefine;

import java.io.Serializable;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:20
 */
public final class ResponseWrapper<T> implements Serializable {
    private Integer result;
    private String code;
    private String message;
    private T data;

    public static <T> ResponseWrapper<T> SUCCESS() {
        return SUCCESS(null);
    }

    public static <T> ResponseWrapper<T> SUCCESS(T data) {
        return fillResult(ResponceCodeUtils.getGlobalSuccResultCode(), GlobalResponseCodeEnum.SUCCESS.getCode(), GlobalResponseCodeEnum.SUCCESS.getMsg(), data);
    }

    public static <T> ResponseWrapper<T> error(CodeDefine responseCodeEnum) {
        return error(responseCodeEnum, null);
    }

    public static <T> ResponseWrapper<T> error(String code, String msg) {
        return error(code, msg, null);
    }

    public static <T> ResponseWrapper<T> error(CodeDefine<String> responseCodeEnum, T data) {
        return error(responseCodeEnum.getCode(), responseCodeEnum.getMsg(), data);
    }

    public static <T> ResponseWrapper<T> error(String code, String msg, T data) {
        return fillResult(ResponceCodeUtils.getGlobalErrResultCode(), code, msg, null);
    }

    private static <T> ResponseWrapper<T> fillResult(Integer responseResult, String responseCode, String responseMsg, T data) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.setResult(responseResult);
        responseWrapper.setCode(responseCode);
        responseWrapper.setMessage(responseMsg);
        responseWrapper.setData(data);
        return responseWrapper;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
