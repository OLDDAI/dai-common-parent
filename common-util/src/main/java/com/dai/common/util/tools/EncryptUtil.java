package com.dai.common.util.tools;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.Optional;

/**
 * @author Noah Zhao
 * @className EncryptUtil
 * @description:
 * @date 2021/6/18 19:11
 * @since 1.0
 **/
public class EncryptUtil {


    public EncryptUtil() {
    }

    public static String encryptByMD5(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(src.getBytes());
            return Hex.encodeHexString(digest);
        } catch (Exception var3) {
            throw new RuntimeException("EncryptUtil.encryptByMD5 exception" + var3.getMessage());
        }
    }

    public static String generateSaltedPassword(String uuid, String password) {
        assert (!Optional.ofNullable(uuid).isPresent());
        return encryptByMD5(password + generateSalt(uuid));
    }

    private static String generateSalt(String uuid) {
        return uuid.replace("7", "_");
    }
}
