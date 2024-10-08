package com.aiyostudio.pokemoninfo.internal.manager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Blank038
 */
public class ActionCooldownManager {
    private static final Map<String, Map<String, Long>> COOLDOWN_MAP = new HashMap<>();

    public static void init() {
        COOLDOWN_MAP.clear();
        COOLDOWN_MAP.put("show", new HashMap<>());
        COOLDOWN_MAP.put("action", new HashMap<>());
    }

    public static boolean isCooldown(String type, String playerName) {
        if (COOLDOWN_MAP.containsKey(type) && COOLDOWN_MAP.get(type).containsKey(playerName)) {
            return System.currentTimeMillis() < COOLDOWN_MAP.get(type).get(playerName);
        }
        return false;
    }

    public static void setCooldown(String type, String playerName, long cooldown) {
        if (COOLDOWN_MAP.containsKey(type)) {
            COOLDOWN_MAP.get(type).put(playerName, System.currentTimeMillis() + cooldown);
        }
    }
}
