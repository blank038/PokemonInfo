package com.aiyostudio.pokemoninfo.internal.command;

import com.aiyostudio.pokemoninfo.internal.cache.PlayerData;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.handler.convert.ConverHandler;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.manager.CacheManager;
import com.aiyostudio.pokemoninfo.internal.view.PartyView;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

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
            return false;
        }
        switch (args[0]) {
            case "reload":
                this.reloadConfig(sender);
                break;
            case "import":
                this.importData(sender, args);
                break;
            default:
                break;
        }
        return false;
    }

    private void reloadConfig(CommandSender sender) {
        if (sender.hasPermission("pokemoninfo.admin")) {
            Configuration.init(plugin);
            sender.sendMessage(I18n.getStrAndHeader("reload"));
        }
    }

    private void importData(CommandSender sender, String[] args) {
        if (!sender.hasPermission("pokemoninfo.admin")) {
            return;
        }
        if (args.length == 1 || !ConverHandler.hasSource(args[1])) {
            sender.sendMessage(I18n.getStrAndHeader("pls-enter-source-storage"));
            return;
        }
        if (args.length == 2 || !ConverHandler.hasSource(args[2])) {
            sender.sendMessage(I18n.getStrAndHeader("pls-enter-target-storage"));
            return;
        }
        if (args[1].equalsIgnoreCase(args[2])) {
            sender.sendMessage(I18n.getStrAndHeader("same-source"));
            return;
        }
        if (ConverHandler.isConverting()) {
            sender.sendMessage(I18n.getStrAndHeader("converting"));
            return;
        }
        PlayerData playerData = CacheManager.find(sender.getName());
        if (playerData == null) {
            CacheManager.push(sender);
            playerData = CacheManager.find(sender.getName());
        }
        if (System.currentTimeMillis() - playerData.getConvertTime() < 10000L) {
            ConverHandler.setConverting(true);
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try {
                    if (ConverHandler.convert(args[1].toLowerCase(), args[2].toLowerCase())) {
                        sender.sendMessage(I18n.getStrAndHeader("convert-complete"));
                        this.plugin.getLogger().info("数据导入成功, 来源: " + args[1] + " 目标: " + args[2]);
                    } else {
                        sender.sendMessage(I18n.getStrAndHeader("convert-data-failed"));
                    }
                } catch (Exception ex) {
                    sender.sendMessage(I18n.getStrAndHeader("convert-data-failed"));
                    this.plugin.getLogger().log(Level.WARNING, ex, () -> "Failed to import data, source: " + args[1] + " target: " + args[2]);
                }
                ConverHandler.setConverting(false);
            });
        } else {
            playerData.setConvertTime(System.currentTimeMillis());
            sender.sendMessage(I18n.getStrAndHeader("convert-warn"));
        }
    }
}
