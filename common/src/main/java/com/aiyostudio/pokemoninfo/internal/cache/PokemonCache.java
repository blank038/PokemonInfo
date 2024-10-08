package com.aiyostudio.pokemoninfo.internal.cache;

import lombok.Getter;


/**
 * @author Blank038
 */
@Getter
public class PokemonCache {
    private final String pokemonId;
    private final Object pokemonData;

    public PokemonCache(String pokemonId,  Object pokemonData) {
        this.pokemonId = pokemonId;
        this.pokemonData = pokemonData;
    }
}
