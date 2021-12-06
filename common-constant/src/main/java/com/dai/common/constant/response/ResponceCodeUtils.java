package com.dai.common.constant.response;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:16
 */
public class ResponceCodeUtils {
    private static final String PROJECT_TAG = "BI";//工程标识
    private static final String LINK = "-";//连接符
    private static final String ERROR = "ERR";//错误标识
    private static final String ERROR_LINK = LINK + ERROR + LINK;//错误标识
    private static final String SUCCESS = "SUC";//成功标识
    private static final String SUCCESS_LINK = LINK + SUCCESS + LINK;//错误标识
    private static final String PROJECT_ERROR_TAG = PROJECT_TAG + ERROR_LINK;
    private static final String PROJECT_SUCCESS_TAG = PROJECT_TAG + SUCCESS_LINK;

    public static String getGlobalResponceSuccTag() {
        return PROJECT_SUCCESS_TAG;
    }

    public static String getGlobalResponceErrTag() {
        return PROJECT_ERROR_TAG;
    }

    public static String getServiceResponceSuccTag(final String serviceTag) {
        return PROJECT_TAG + LINK + serviceTag + LINK;
    }

    public static String getServiceResponceErrTag(final String serviceTag) {
        return PROJECT_TAG + LINK + serviceTag + LINK;
    }


    public static Integer getGlobalSuccResultCode() {
        return GlobalResponseResultEnum.SUCCESS.getCode();
    }

    public static Integer getGlobalErrResultCode() {
        return GlobalResponseResultEnum.ERROR.getCode();
    }
}
