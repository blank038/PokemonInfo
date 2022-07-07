package com.mc9y.pokemoninfo.i18n;

import com.mc9y.blank038api.util.common.CommonUtil;
import com.mc9y.pokemoninfo.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

/**
 * @author Blank038
 * @since 2021-06-03
 */
public class I18n {
    private static final String[] LANGUAGES = {"zh_CN.yml", "en_US.yml"};
    private static I18n langData;

    private final HashMap<String, String> OPTIONS = new HashMap<>();
    private final String language;

    public static I18n inst() {
        return langData;
    }

    public I18n(String language) {
        langData = this;
        this.language = language;
        File folder = new File(Main.getInstance().getDataFolder(), "lang");
        if (!folder.exists()) {
            folder.mkdir();
            for (String lang : LANGUAGES) {
                File tar = new File(folder, lang);
                CommonUtil.outputFileTool(Main.getInstance().getResource("lang/" + lang), tar);
            }
        }
        // 读取语言配置文件
        File file = new File(folder, language + ".yml");
        if (!file.exists()) {
            file = new File(folder, "zh_CN.yml");
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            if (data.isString(key)) {
                this.OPTIONS.put(key, ChatColor.translateAlternateColorCodes('&', data.getString(key, "")));
            }
        }
    }

    public String getLanguage() {
        return this.language;
    }

    public String getOption(String key) {
        if (this.OPTIONS.containsKey(key)) {
            return this.OPTIONS.get(key);
        }
        return "";
    }
}
