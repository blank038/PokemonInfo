package com.mc9y.pokemoninfo.command;

import com.mc9y.pokemoninfo.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author Blank038
 */
public class BaseCommand {
    private FileConfiguration config;
    private final File f;
    public String prefix;

    public BaseCommand(String type) {
        f = new File(Main.getInstance().getDataFolder(), type + ".yml");
        if (!f.exists()) {
            Main.getInstance().saveResource(type + ".yml", true);
        }
        reloadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(f);
        prefix = config.getString("Prefix").replace("&", "§");
    }
}