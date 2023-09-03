package com.aiyostudio.pokemoninfo.util;

import java.util.Base64;

public class Base64Util {

    public static String encode(String text) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(text.getBytes());
    }

    public static String decode(String text) {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(text));
    }
}
