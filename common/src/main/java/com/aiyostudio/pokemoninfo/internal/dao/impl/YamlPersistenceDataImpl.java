package com.aiyostudio.pokemoninfo.internal.dao.impl;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Blank038
 */
public class YamlPersistenceDataImpl extends AbstractPersistenceDataImpl {

    public YamlPersistenceDataImpl(ConfigurationSection options, boolean silent) {
        super(options, silent);
        this.checkAndGetFolder();
    }

    public File checkAndGetFolder() {
        File data = new File(PokemonInfo.getInstance().getDataFolder(), "PokeEggs");
        if (!data.exists()) {
            data.mkdir();
        }
        return data;
    }

    @Override
    public PokemonCache getPokemonCache(String pokemonId) {
        File file = new File(this.checkAndGetFolder(), pokemonId + ".pke");
        if (file.exists()) {
            return new PokemonCache(pokemonId, PokemonInfo.getModule().fileToPokemon(file));
        }
        return null;
    }

    @Override
    public boolean removePokemonCache(String pokemonId) {
        File file = new File(this.checkAndGetFolder(), pokemonId + ".pke");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    @Override
    public boolean addPokemonCache(PokemonCache pokemonCache) {
        File file = new File(this.checkAndGetFolder(), pokemonCache.getPokemonId() + ".pke");
        if (file.exists()) {
            return false;
        }
        return PokemonInfo.getModule().writePokemonToFile(pokemonCache, file);
    }

    @Override
    public List<PokemonCache> findAll() {
        return Arrays.stream(this.checkAndGetFolder().listFiles())
                .map((file) -> {
                    String pokemonId = file.getName().substring(0, file.getName().length() - 4);
                    return new PokemonCache(pokemonId, PokemonInfo.getModule().fileToPokemon(file));
                })
                .collect(Collectors.toList());
    }
}
