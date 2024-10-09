package com.aiyostudio.pokemoninfo.internal.enums;

import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.view.PartyView;
import com.aiyostudio.pokemoninfo.internal.view.PokemonConvertView;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * @author Blank038
 */
public enum ActionTypeEnum {
    CONVERT((player, slot) -> {
        if (Configuration.getPokeEggModuleConfig().getBoolean("settings.enable")) {
            PokemonConvertView.open(player, slot);
        }
    }),
    SHOW((player, slot) -> {
        PartyView.showPokemon(player, slot, PartyView.getData().getConfigurationSection("show-setting"));
    }),
    NONE((player, slot) -> {});

    private final BiConsumer<Player, Integer> action;

    ActionTypeEnum(BiConsumer<Player, Integer> consumer) {
        this.action = consumer;
    }

    public void run(Player player, Integer slot) {
        this.action.accept(player, slot);
    }
}
