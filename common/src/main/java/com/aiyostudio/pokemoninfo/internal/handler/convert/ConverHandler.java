package com.aiyostudio.pokemoninfo.internal.handler.convert;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.dao.impl.MysqlPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.dao.impl.SQLitePersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.dao.impl.YamlPersistenceDataImpl;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class ConverHandler {
    private static final Map<String, Class<? extends AbstractPersistenceDataImpl>> CLASS_MAP = new HashMap<>();
    @Setter
    @Getter
    private static boolean converting = false;

    static {
        CLASS_MAP.put("mysql", MysqlPersistenceDataImpl.class);
        CLASS_MAP.put("yaml", YamlPersistenceDataImpl.class);
        CLASS_MAP.put("sqlite", SQLitePersistenceDataImpl.class);
    }

    public static boolean hasSource(String source) {
        return CLASS_MAP.containsKey(source.toLowerCase());
    }

    public static boolean convert(String source, String target) {
        FileConfiguration config = PokemonInfo.getInstance().getConfig();
        Class<? extends AbstractPersistenceDataImpl> sourceClass = CLASS_MAP.get(source.toLowerCase()),
                targetClass = CLASS_MAP.get(target.toLowerCase());
        ConfigurationSection sourceConfig = config.getConfigurationSection("convert-option." + source),
                targetConfig = config.getConfigurationSection("convert-option." + target);
        if (targetConfig == null || sourceConfig == null) {
            return false;
        }
        try {
            AbstractPersistenceDataImpl sourceImpl = sourceClass.getConstructor(ConfigurationSection.class, boolean.class)
                    .newInstance(sourceConfig, true);
            AbstractPersistenceDataImpl targetImpl = targetClass.getConstructor(ConfigurationSection.class, boolean.class)
                    .newInstance(targetConfig, true);
            sourceImpl.findAll().forEach(targetImpl::addPokemonCache);
            return true;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            PokemonInfo.getInstance().getLogger().log(Level.WARNING, e, () -> "Failed to create persistence data object.");
            return false;
        }
    }
}
