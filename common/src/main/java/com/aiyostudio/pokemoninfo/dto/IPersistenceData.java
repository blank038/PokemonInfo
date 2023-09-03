package com.aiyostudio.pokemoninfo.dto;

import com.aiyostudio.pokemoninfo.cache.PokemonCache;

/**
 * @author Blank038
 */
public interface IPersistenceData {

    PokemonCache getPokemonCache(String pokemonId);

    boolean removePokemonCache(String pokemonId);

    boolean addPokemonCache(PokemonCache pokemonCache);
}
