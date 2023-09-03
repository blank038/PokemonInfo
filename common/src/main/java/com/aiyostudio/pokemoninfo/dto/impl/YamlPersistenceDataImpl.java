package com.aiyostudio.pokemoninfo.dto.impl;

import com.aiyostudio.pokemoninfo.PokemonInfo;
import com.aiyostudio.pokemoninfo.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.dto.AbstractPersistenceDataImpl;

import java.io.File;

/**
 * @author Blank038
 */
public class YamlPersistenceDataImpl extends AbstractPersistenceDataImpl {

    public YamlPersistenceDataImpl() {
        super();
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
}
