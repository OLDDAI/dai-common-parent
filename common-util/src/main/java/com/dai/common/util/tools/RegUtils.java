package com.dai.common.util.tools;

import com.dai.common.util.constant.RegConstant;
import com.dai.common.util.tools.lang3.StringUtils;
import java.util.regex.Pattern;

/**
 * 正则校验工具类
 */
public class RegUtils {
    /**
     * 手机号码校验
     */
    public static boolean checkPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_mobile, phone);
    }


    /**
     * 数字校验
     */
    public static boolean checkNumber(String number) {
        if (StringUtils.isBlank(number)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_number, number);
    }


    /**
     * 密码校验
     */
    public static boolean checkPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_password, password);
    }


    /**
     * 验证码校验
     *
     * @param verifyCode
     * @return
     */
    public static boolean checkVerifyCode(String verifyCode) {
        if (StringUtils.isBlank(verifyCode)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_verify_code, verifyCode);
    }

    /**
     * 网址格式校验
     *
     * @param url
     * @return
     */
    public static boolean checkUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_url, url);
    }

    /**
     * gltf格式楼层地图文件地址
     *
     * @param mapurl
     * @return
     */
    public static boolean checkMapUrl(String mapurl) {
        if (StringUtils.isBlank(mapurl)) {
            return false;
        }
        if (!mapurl.endsWith(RegConstant.map_url_suffix)) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_url, mapurl);
    }

    /**
     * name 校验
     *
     * @param name
     * @return
     */
    public static boolean checkName(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        if (name.startsWith(" ") || name.endsWith(" ")) {
            return false;
        }
        return name.length() <= 60;
    }

    /**
     * PersonName 校验
     *
     * @param personName
     * @return
     */
    public static boolean checkPersonName(String personName) {
        if (StringUtils.isBlank(personName)) {
            return false;
        }
        if (personName.startsWith(" ") || personName.endsWith(" ")) {
            return false;
        }
        return Pattern.matches(RegConstant.reg_name, personName);
    }

    /**
     * 备注/
     * 简介/描述类 1-255字符
     *
     * @param content
     * @return
     */
    public static boolean checkContent(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        return content.length() <= 255;

    }


}
