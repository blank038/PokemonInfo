package com.mc9y.pokemoninfo.command;

import com.mc9y.blank038api.Blank038API;
import com.mc9y.pokemonapi.api.event.CustomEvent;
import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.data.PokeEgg;
import com.mc9y.pokemoninfo.i18n.I18n;
import com.mc9y.pokemoninfo.utils.MessageUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.pokemon.SpecFlag;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Blank038
 */
public class PokeEggCommand extends BaseCommand
        implements CommandExecutor {

    public PokeEggCommand(String type) {
        super(type);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (this.getConfig().getBoolean("enable")) {
            commandSender.sendMessage(prefix + I18n.inst().getOption("model_closed"));
            return true;
        }
        if (strings.length == 0) {
            this.sendHelp(commandSender);
        } else {
            switch (strings[0]) {
                case "create":
                    if (commandSender instanceof Player) {
                        this.convertPokemonToPhoto((Player) commandSender, strings);
                    }
                    break;
                case "reload":
                    if (commandSender.hasPermission("pokemoninfo.admin")) {
                        this.reloadConfig();
                        commandSender.sendMessage(prefix + I18n.inst().getOption("reload"));
                    }
                    break;
                case "get":
                    this.get(commandSender, strings);
                    break;
                default:
                    this.sendHelp(commandSender);
                    break;
            }
        }
        return false;
    }


    public void get(CommandSender sender, String[] args) {
        if (sender instanceof Player && sender.hasPermission("pokemoninfo.admin")) {
            if (args.length > 1) {
                PokeEgg pokeEgg = Main.getInstance().getDataInterface().get(args[1]);
                if (pokeEgg != null) {
                    Player player = (Player) sender;
                    ItemStack itemStack = getPokeEggItem(pokeEgg.getPokemon(), false, -1, null);
                    player.getInventory().addItem(itemStack);
                    sender.sendMessage(prefix + I18n.inst().getOption("send"));
                } else {
                    sender.sendMessage(prefix + I18n.inst().getOption("sprite_not_exists"));
                }
            } else {
                sender.sendMessage(prefix + I18n.inst().getOption("input_sprite_key"));
            }
        }
    }

    public void convertPokemonToPhoto(Player player, String[] strings) {
        if (!player.hasPermission("pokemoninfo.pokeegg")) {
            player.sendMessage(prefix + I18n.inst().getOption("no_perms"));
            return;
        }
        if (strings.length == 1) {
            player.sendMessage(prefix + I18n.inst().getOption("wrong_slot"));
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(strings[1]);
        } catch (Exception e) {
            player.sendMessage(prefix + I18n.inst().getOption("wrong_slot"));
            return;
        }
        if (slot > 0 && slot < 7) {
            slot--;
            PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
            if (pps.getTeam().size() <= 1) {
                player.sendMessage(prefix + I18n.inst().getOption("least_one_left"));
                return;
            }
            Pokemon pokemon = pps.get(slot);
            if (pokemon == null || pokemon.isEgg()) {
                player.sendMessage(prefix + I18n.inst().getOption("wrong_pokemon"));
                return;
            }
            if (getConfig().getStringList("BlackList").contains(pokemon.getSpecies().getPokemonName())) {
                player.sendMessage(prefix + I18n.inst().getOption("convert_deny"));
                return;
            }
            if (((SpecFlag) PokemonSpec.getSpecForKey("untradeable")).matches(pokemon)) {
                player.sendMessage(prefix + I18n.inst().getOption("convert_deny"));
                return;
            }
            if (!getConfig().getBoolean("AllowColor") && pokemon.getNickname() != null && pokemon.getNickname().contains("§")) {
                player.sendMessage(prefix + I18n.inst().getOption("deny_rename_pokemon"));
                return;
            }
            int ivs = (int) Arrays.stream(pokemon.getIVs().getArray()).filter((s) -> s >= 31).count();
            if (ivs > getConfig().getInt("max_ivs", 7)) {
                player.sendMessage(prefix + I18n.inst().getOption("max_ivs"));
                return;
            }
            CustomEvent customEvent = new CustomEvent("PokeCompoundEvent", pokemon, player);
            Bukkit.getPluginManager().callEvent(customEvent);
            if (customEvent.isCancelled()) {
                return;
            }
            ItemStack item = this.getPokeEggItem(pokemon, true, slot, pps);
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item.clone());
                player.sendMessage(prefix + I18n.inst().getOption("inv_full"));
            } else {
                player.getInventory().addItem(item.clone());
                player.sendMessage(prefix + I18n.inst().getOption("successfully_converted"));
            }
        } else {
            player.sendMessage(prefix + I18n.inst().getOption("out_number"));
        }
    }

    public void sendHelp(CommandSender sender) {
        for (String message : getConfig().getStringList("Help." + (sender.hasPermission("poketools.admin") ? "admin" : "default"))) {
            sender.sendMessage(message.replace("&", "§"));
        }
    }

    /**
     * 获取精灵相片
     */
    public ItemStack getPokeEggItem(Pokemon pokemon, boolean remove, int slot, PlayerPartyStorage pps) {
        ItemStack item = Blank038API.getPokemonAPI().getSpriteHelper().getSpriteItem(pokemon);
        String localizedName = Blank038API.getPokemonAPI().getLang().getString("pixelmon." +
                pokemon.getSpecies().name.toLowerCase() + ".name");
        List<String> lores = new ArrayList<>();
        for (String lore : Blank038API.getPokemonAPI().getStatsHelper().format(pokemon, getConfig().getStringList("Lore"))) {
            lores.add(MessageUtil.formatText(pokemon, lore).replace("%species%", pokemon.getSpecies().getPokemonName())
                    .replace("%species_lang%", localizedName));
        }
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(lores);
        itemMeta.setDisplayName("§e" + localizedName);
        item.setItemMeta(itemMeta);
        if (remove) {
            pps.retrieveAll();
            pps.set(slot, null);
        }
        File f = new File(Main.getInstance().getDataFolder() + "/PokeEggs/", UUID.randomUUID() + ".pke");
        PokeEgg pe = new PokeEgg(f, pokemon);
        net.minecraft.server.v1_12_R1.ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsitem.hasTag() ? nmsitem.getTag() : new NBTTagCompound();
        assert compound != null;
        compound.set("PokeEggUUID", new NBTTagString(pe.getUUID()));
        nmsitem.setTag(compound);
        item = CraftItemStack.asBukkitCopy(nmsitem);
        return item;
    }
}
