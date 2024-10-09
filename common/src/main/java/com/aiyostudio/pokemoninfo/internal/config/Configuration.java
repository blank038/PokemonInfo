package com.aiyostudio.pokemoninfo.internal.config;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.view.PartyView;
import com.aiyostudio.pokemoninfo.internal.view.PokemonConvertView;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final Map<String, FileConfiguration> CONFIGURATION_MAP = new HashMap<>();

    public static void init(PokemonInfo plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        CONFIGURATION_MAP.clear();

        File moduleFolder = new File(plugin.getDataFolder(), "module");
        if (!moduleFolder.exists()) {
            PokemonInfo.getInstance().saveResource("module/pi.yml", "module/pi.yml");
            PokemonInfo.getInstance().saveResource("module/pokeegg.yml", "module/pokeegg.yml");
        }
        for (File i : moduleFolder.listFiles()) {
            String fileName = i.getName().substring(0, i.getName().indexOf(".yml"));
            CONFIGURATION_MAP.put(fileName.toLowerCase(), YamlConfiguration.loadConfiguration(i));
        }
        // 初始化本地化文件
        new I18n(PokemonInfo.getInstance().getConfig().getString("language", "zh_CN"));
        // 初始化面板文件
        PartyView.init();
        PokemonConvertView.init();
    }

    public static FileConfiguration getModuleConfig(String moduleName) {
        return CONFIGURATION_MAP.get(moduleName);
    }

    public static FileConfiguration getPIModuleConfig() {
        return CONFIGURATION_MAP.get("pi");
    }

    public static FileConfiguration getPokeEggModuleConfig() {
        return CONFIGURATION_MAP.get("pokeegg");
    }

    public static FileConfiguration getConfig() {
        return PokemonInfo.getInstance().getConfig();
    }
}
