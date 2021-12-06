package com.dai.common.constant.utils;

import com.dai.common.constant.model.JwtUserModel;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 15:26
 */
public class UserSessionUtils {
    private static final ThreadLocal<JwtUserModel> jwtUserModelThreadLocal = new ThreadLocal<>();

    public UserSessionUtils() {
    }

    public static void setUser(JwtUserModel jwtUserModel) {
        jwtUserModelThreadLocal.set(jwtUserModel);
    }

    public static JwtUserModel getUser() {
        return jwtUserModelThreadLocal.get();
    }

    public static void clean() {
        jwtUserModelThreadLocal.remove();
    }
}
