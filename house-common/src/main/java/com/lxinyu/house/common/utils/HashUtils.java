package com.lxinyu.house.common.utils;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

public class HashUtils {

    private static final HashFunction FUNCTION = Hashing.md5();

    /**
     * 盐的作用是为了防止某些简单的MD5密码被暴力破解
     * 有些简单的密码其MD5的值是固定的，可以暴力破解，加盐后不容易被破解
     */
    private static final String SALT = "lxinyu.com";

    public static String encryPassword(String password){
        HashCode hashCode = FUNCTION.hashString(password+SALT, Charset.forName("utf-8"));

        return hashCode.toString();
    }
}
