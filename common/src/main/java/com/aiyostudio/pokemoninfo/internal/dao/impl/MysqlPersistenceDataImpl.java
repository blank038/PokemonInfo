package com.aiyostudio.pokemoninfo.internal.dao.impl;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.debug.DebugControl;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.util.Base64Util;
import com.aystudio.core.bukkit.util.mysql.MySqlStorageHandler;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class MysqlPersistenceDataImpl extends AbstractPersistenceDataImpl {
    private static MySqlStorageHandler storageHandler;

    public MysqlPersistenceDataImpl(ConfigurationSection options, boolean silent) {
        super(silent);
        String[] array = {
                "CREATE TABLE IF NOT EXISTS pokemoninfo (uuid VARCHAR(50) NOT NULL, data TEXT, PRIMARY KEY ( uuid ))"
        };
        storageHandler = new MySqlStorageHandler(PokemonInfo.getInstance(), options.getString(".url"),
                options.getString("user"), options.getString("password"), array);
        storageHandler.setCheckConnection(true);
        storageHandler.setReconnectionQueryTable("pokemoninfo");
    }

    @Override
    public PokemonCache getPokemonCache(String pokemonId) {
        AtomicReference<String> nbtData = new AtomicReference<>(null);
        storageHandler.connect((statement) -> {
            try {
                statement.setString(1, pokemonId);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String data = resultSet.getString("data");
                    if (data != null) {
                        nbtData.set(data);
                        break;
                    }
                }
                resultSet.close();
            } catch (SQLException e) {
                DebugControl.log(Level.SEVERE, e.toString());
            }
        }, "SELECT data FROM pokemoninfo WHERE uuid=?");
        if (nbtData.get() == null) {
            return null;
        }
        String data = Base64Util.decode(nbtData.get());
        return new PokemonCache(pokemonId, PokemonInfo.getModule().stringToPokemon(data));
    }

    @Override
    public boolean removePokemonCache(String pokemonId) {
        AtomicBoolean result = new AtomicBoolean(false);
        storageHandler.connect((statement) -> {
            try {
                statement.setString(1, pokemonId);
                if (statement.executeUpdate() > 0) {
                    result.set(true);
                }
            } catch (SQLException e) {
                DebugControl.log(Level.SEVERE, e.toString());
            }
        }, "DELETE FROM pokemoninfo WHERE uuid=?");
        return result.get();
    }

    @Override
    public boolean addPokemonCache(PokemonCache pokemonCache) {
        AtomicBoolean result = new AtomicBoolean(false);
        storageHandler.connect((statement) -> {
            try {
                String data = PokemonInfo.getModule().pokemonToString(pokemonCache.getPokemonData());
                statement.setString(1, pokemonCache.getPokemonId());
                statement.setString(2, Base64Util.encode(data));
                if (statement.executeUpdate() > 0) {
                    result.set(true);
                }
            } catch (SQLException e) {
                DebugControl.log(Level.SEVERE, e.toString());
            }
        }, "INSERT INTO pokemoninfo (uuid,data) VALUES (?,?)");
        return result.get();
    }

    @Override
    public List<PokemonCache> findAll() {
        List<PokemonCache> result = new ArrayList<>();
        storageHandler.connect((statement) -> {
            try {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String uuid = resultSet.getString(1);
                    String data = resultSet.getString(2);
                    if (uuid != null && data != null) {
                        String decodeStr = Base64Util.decode(data);
                        PokemonCache pokemonCache = new PokemonCache(uuid, PokemonInfo.getModule().stringToPokemon(decodeStr));
                        result.add(pokemonCache);
                    }
                }
            } catch (SQLException e) {
                DebugControl.log(Level.SEVERE, e.toString());
            }
        }, "SELECT uuid,data FROM pokemoninfo;");
        return result;
    }
}
