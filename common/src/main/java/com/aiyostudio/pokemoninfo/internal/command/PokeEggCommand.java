package com.aiyostudio.pokemoninfo.internal.command;

import com.aiyostudio.pokemoninfo.api.PokemonInfoApi;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
public class PokeEggCommand implements CommandExecutor {
    private final PokemonInfo plugin = PokemonInfo.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) return false;
        if (args[0].equals("create")) {
            this.createPokeEgg(sender, args);
        }
        return false;
    }

    private void createPokeEgg(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        if (args.length == 1) {
            sender.sendMessage(I18n.getStrAndHeader("pls-enter-slot"));
            return;
        }
        try {
            int slot = Integer.parseInt(args[1]);
            if (slot < 1 || slot > 6) {
                return;
            }
            if (PokemonInfoApi.convertByDefault((Player) sender, --slot)) {
                sender.sendMessage(I18n.getStrAndHeader("convert"));
            } else {
                sender.sendMessage(I18n.getStrAndHeader("denied"));
            }
        } catch (Exception ignored) {
            sender.sendMessage(I18n.getStrAndHeader("wrong-number"));
        }
    }
}
