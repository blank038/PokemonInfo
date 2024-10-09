package com.aiyostudio.pokemoninfo.internal.dao;

import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;

import java.util.List;

/**
 * @author Blank038
 */
public interface IPersistenceData {

    PokemonCache getPokemonCache(String pokemonId);

    boolean removePokemonCache(String pokemonId);

    boolean addPokemonCache(PokemonCache pokemonCache);

    List<PokemonCache> findAll();
}
