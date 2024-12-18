package com.aiyostudio.pokemoninfo.internal.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Blank038
 */
public class TextUtil {
    private static final Pattern PATTERN = Pattern.compile("#[A-f0-9]{6}");
    public static final Pattern IVS_PATTERN = Pattern.compile("%IVS_[a-zA-Z]+%");

    public static String formatHexColor(String message) {
        String copy = message;
        Matcher matcher = PATTERN.matcher(copy);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            copy = copy.replace(color, String.valueOf(ChatColor.of(color)));
        }
        return ChatColor.translateAlternateColorCodes('&', copy);
    }
}
