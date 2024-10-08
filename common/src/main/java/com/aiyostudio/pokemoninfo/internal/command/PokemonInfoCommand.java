package com.aiyostudio.pokemoninfo.internal.command;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.view.PartyView;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Blank038
 */
public class PokemonInfoCommand implements CommandExecutor {
    private final PokemonInfo plugin = PokemonInfo.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                PartyView.open((Player) sender);
            }
        } else if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("pokemoninfo.admin")) {
            Configuration.init(plugin);
            sender.sendMessage(I18n.getStrAndHeader("reload"));
        }
        return false;
    }
}
