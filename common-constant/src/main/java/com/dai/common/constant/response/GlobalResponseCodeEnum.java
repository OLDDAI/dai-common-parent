package com.dai.common.constant.response;

import com.dai.common.constant.enums.CodeDefine;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:17
 */
public enum GlobalResponseCodeEnum implements CodeDefine<String> {
    /*
     * 全局系统异常错误码0-1999
     */

    SUCCESS(ResponceCodeUtils.getGlobalResponceSuccTag()+"200", "success"),
    ERROR(ResponceCodeUtils.getGlobalResponceErrTag()+"500", "system error"),
    NOT_FOUND(ResponceCodeUtils.getGlobalResponceErrTag()+"404", "not found"),
    PARAMETER_ERROR(ResponceCodeUtils.getGlobalResponceErrTag()+"600", "invalid parameter"),
    ILLEGAL_OPERATION(ResponceCodeUtils.getGlobalResponceErrTag()+"601", "illegal operation"),
    SERVICE_UNAVAILABLE(ResponceCodeUtils.getGlobalResponceErrTag()+"602", "service unavailable"),
    SERVER_BUSY(ResponceCodeUtils.getGlobalResponceErrTag()+"603", "service busy"),
    SESSION_INVALID(ResponceCodeUtils.getGlobalResponceErrTag()+"604", "invalid session"),
    REQUEST_METHOD_NOT_SUPPORTED(ResponceCodeUtils.getGlobalResponceErrTag()+"605", "request method not support"),
    UPLOAD_SIZE_LIMIT(ResponceCodeUtils.getGlobalResponceErrTag()+"606", "request file is too large"),
    FLOW_LIMITING(ResponceCodeUtils.getGlobalResponceErrTag()+"607", "flow limiting"),
    ;
    private String code;
    private String desc;

    GlobalResponseCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return desc;
    }
}
