package com.aiyostudio.pokemoninfo.command;

import com.aiyostudio.pokemoninfo.PokemonInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Blank038
 */
public class PokeEggCommand implements CommandExecutor {
    private final PokemonInfo plugin = PokemonInfo.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
