package com.mc9y.pokemoninfo.utils;

import com.mc9y.combatpower.api.CombatPowerAPI;
import com.mc9y.pokemonapi.api.pokemon.PokemonUtil;
import com.mc9y.pokestar.PokeStarAPI;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.bukkit.Bukkit;

/**
 * @author Blank038
 * @since 2021-12-03
 */
public class MessageUtil {

    public static String formatText(Pokemon pokemon, String text) {
        if (Bukkit.getPluginManager().getPlugin("PokeStar") != null && Bukkit.getPluginManager().getPlugin("PokeStar").isEnabled()) {
            PokeStarAPI psa = new PokeStarAPI();
            int star = psa.getPokemonStar(pokemon.getSpecies().name());
            text = text.replace("%star%", psa.getPokeShowName(star));
        }
        if (Bukkit.getPluginManager().getPlugin("CombatPower") != null && Bukkit.getPluginManager().getPlugin("CombatPower").isEnabled()) {
            CombatPowerAPI cpa = com.mc9y.combatpower.Main.getCombatPowerAPI();
            text = text.replace("%combat_power%", cpa.getPokemonCombatPower(pokemon) + "");
        }
        return text.replace("%pokemon_name%", PokemonUtil.getPokemonName(pokemon.getSpecies()));
    }
}
