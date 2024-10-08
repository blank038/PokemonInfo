package com.aiyostudio.pokemoninfo.internal.i18n;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.util.TextUtil;
import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AiYo Studio
 * @since 1.0.0 - Blank038 - 2023-07-22
 */
public class I18n {
    private static final String[] LANGUAGES = {"zh_CN.yml"};
    private static I18n i18n;

    private final Map<String, String> stringOptions = new HashMap<>();
    private final Map<String, List<String>> arrayOptions = new HashMap<>();
    @Getter
    private String language, header;

    public static I18n inst() {
        return i18n;
    }

    public I18n(String language) {
        this.init(language);
    }

    public void init(String language) {
        i18n = this;
        this.language = language;
        this.stringOptions.clear();
        this.arrayOptions.clear();
        File folder = new File(PokemonInfo.getInstance().getDataFolder(), "language");
        if (!folder.exists()) {
            folder.mkdir();
            for (String lang : LANGUAGES) {
                File tar = new File(folder, lang);
                CommonUtil.outputFileTool(PokemonInfo.getInstance().getResource("language/" + lang), tar);
            }
        }
        // 读取语言配置文件
        File file = new File(folder, language + ".yml");
        if (!file.exists()) {
            file = new File(folder, "zh_CN.yml");
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(true)) {
            if (data.isString(key)) {
                this.stringOptions.put(key, TextUtil.formatHexColor(data.getString(key)));
            } else if (data.isList(key)) {
                this.arrayOptions.put(key, data.getStringList(key).stream()
                        .map(TextUtil::formatHexColor)
                        .collect(Collectors.toList()));
            }
        }
        this.header = "prefix";
    }

    public static String getStrAndHeader(String key) {
        if (i18n.header == null) {
            return I18n.getOption(key);
        }
        return I18n.getOption(i18n.header, key);
    }

    public static String getOption(String header, String key) {
        return i18n.stringOptions.getOrDefault(header, "") + i18n.stringOptions.getOrDefault(key, "");
    }

    public static String getOption(String key) {
        if (i18n.stringOptions.containsKey(key)) {
            return i18n.stringOptions.get(key);
        }
        return "";
    }

    public static List<String> getArrayOption(String key) {
        if (i18n.arrayOptions.containsKey(key)) {
            return new ArrayList<>(i18n.arrayOptions.get(key));
        }
        return Lists.newArrayList();
    }
}