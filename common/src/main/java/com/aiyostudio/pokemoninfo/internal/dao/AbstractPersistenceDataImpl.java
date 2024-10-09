package com.aiyostudio.pokemoninfo.internal.dao;

import com.aiyostudio.pokemoninfo.internal.dao.impl.MysqlPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.dao.impl.SQLitePersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.dao.impl.YamlPersistenceDataImpl;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Blank038
 */
public abstract class AbstractPersistenceDataImpl implements IPersistenceData {
    @Getter
    private static IPersistenceData instance;

    public AbstractPersistenceDataImpl(ConfigurationSection options, boolean silent) {
        this(silent);
    }

    public AbstractPersistenceDataImpl(boolean silent) {
        if (!silent) {
            instance = this;
        }
    }

    public static IPersistenceData of(ConfigurationSection options) {
        if (options == null) {
            return new YamlPersistenceDataImpl(null, false);
        }
        switch (options.getString("type", "").toLowerCase()) {
            case "mysql":
                return new MysqlPersistenceDataImpl(options, false);
            case "sqlite":
                return new SQLitePersistenceDataImpl(options, false);
            case "yaml":
            default:
                return new YamlPersistenceDataImpl(null, false);
        }
    }
}
