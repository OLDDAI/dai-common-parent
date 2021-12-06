package com.dai.common.constant.model;

import java.time.LocalDateTime;

/**
 * @Author: OLDDAI
 * @Date: 2021/10/25 14:59
 */
public class JwtUserModel {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 是否是管理员
     */
    private Boolean isAdmin;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 浏览器
     */
    private String browser;

    /**
     * IP
     */
    private String ip;

    /**
     * 地址
     */
    private String address;

    /**
     * token
     */
    private String key;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
