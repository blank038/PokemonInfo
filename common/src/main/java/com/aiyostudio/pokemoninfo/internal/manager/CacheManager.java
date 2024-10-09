package com.aiyostudio.pokemoninfo.internal.manager;

import com.aiyostudio.pokemoninfo.internal.cache.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Blank038
 */
public class CacheManager {
    private static final Map<String, PlayerData> PLAYER_DATA_MAP = new HashMap<>();

    public static void push(CommandSender sender) {
        if (sender.hasPermission("pokemoninfo.admin")) {
            PLAYER_DATA_MAP.put(sender.getName(), new PlayerData());
        }
    }

    public static void pop(String name) {
        PLAYER_DATA_MAP.remove(name);
    }

    public static PlayerData find(String name) {
        return PLAYER_DATA_MAP.getOrDefault(name, null);
    }
}
