package com.dai.common.util.constant;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 16:01
 * 正则常量管理
 */
public class RegConstant {
    /**
     * 手机号正则校验
     */
    public static final String reg_mobile = "^1[3-9]\\d{9}$";
    /**
     * 验证码 正则校验
     */
    public static final String reg_verify_code = "^\\d{6}$";

    /**
     * 6-18位数字加字母密码 正则校验
     */
    public static final String reg_password = "(?![0-9A-Z]+$)(?![0-9a-z]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,18}$";

    /**
     * 数字 正则校验
     */
    public static final String reg_number = "^[1-9]\\d*$";

    /**
     * 网址格式校验
     */
    public static final String reg_url = "^(https?|ftp|file)://.*";

    /**
     * 楼层地图格式
     */
    public static final String map_url_suffix = ".gltf";
    /**
     * 名称校验
     */
    public static final String reg_name = "[\\u4e00-\\u9fa5_a-zA-Z0-9_ ]{1,32}";
}
