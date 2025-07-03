package com.kaotu.base.utils;

import com.kaotu.base.exception.BaseException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

@Slf4j
public class PasswordUtil {

    /**
     * 密码格式正则表达式.
     * 要求：
     * 1. 长度在 6 到 20 位之间
     * 2. 必须包含至少一个大写字母
     * 3. 必须包含至少一个小写字母
     */
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z]).{6,20}$";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * 验证密码格式是否符合要求
     *
     * @param password 待验证的密码
     * @return 如果格式正确返回 true，否则返回 false
     */
    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private static final String USER_ID_REGEX = "^[a-zA-Z0-9]{6,20}$";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_REGEX);

    public static boolean isUserIdValid(String userId) {
        if (userId == null) {
            return false;
        }
        return USER_ID_PATTERN.matcher(userId).matches();
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 将密码和盐结合
            String text = password + salt;
            final byte[] hashbytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte hashbyte : hashbytes) {
                String hex = Integer.toHexString(0xff & hashbyte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("密码加密失败", e);
            throw new BaseException("系统错误，无法加密密码");
        }
    }
}
