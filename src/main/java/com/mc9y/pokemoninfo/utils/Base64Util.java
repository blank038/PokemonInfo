package com.mc9y.pokemoninfo.utils;

import java.util.Base64;

public class Base64Util {

    /**
     * 加密数据, 返回一个字符串
     */
    public static String encode(String text) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(text.getBytes());
    }

    /**
     * 解密数据, 返回一个 JsonObject
     */
    public static String decode(String text) {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(text));
    }
}
