package com.aiyostudio.pokemoninfo.interfaces;

import com.aiyostudio.pokemoninfo.cache.PokemonCache;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface IModule<T> {

    void run();

    T fileToPokemon(File file);

    T stringToPokemon(String str);

    String pokemonToString(T pokemon);

    boolean writePokemonToFile(PokemonCache pokemonCache, File file);

    boolean isNullOrEgg(UUID uuid, int pokemonSlot);

    boolean isShiny(T pokemon);

    T getPokemon(UUID uuid, int pokemonSlot);

    String getPokemonTranslationName(T pokemonObj);

    String getSpecies(T pokemonObj);

    int getIVStoreValue(T pokemonObj);

    String getPokemonCustomName(T pokemonObj);

    List<String> formatStats(T pokemonObj, List<String> stats);

    ItemStack getPokemonSpriteItem(T pokemonObj);

    void setPartyPokemon(UUID uuid, int pokemonSlot, T pokemon);

    void addPokemon(UUID uuid, T pokemon);

    int getPartyPokemonCount(UUID uuid);
}
