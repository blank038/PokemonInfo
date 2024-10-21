package com.aiyostudio.pokemoninfo.api;

import com.aiyostudio.pokemoninfo.api.event.PlayerConvertPokemonEvent;
import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.manager.CacheManager;
import com.aiyostudio.pokemoninfo.internal.view.PokemonConvertView;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Blank038
 */
public class PokemonInfoApi {

    public static boolean convertByDefault(Player player, int pokemonSlot) {
        if (PokemonInfo.getModule().isNullOrEgg(player.getUniqueId(), pokemonSlot)) {
            return false;
        }
        FileConfiguration configuration = Configuration.getConvertModuleConfig();
        Object pokemonObj = PokemonInfo.getModule().getPokemon(player.getUniqueId(), pokemonSlot);
        String species = PokemonInfo.getModule().getSpecies(pokemonObj);

        if (configuration.getStringList("black-list").contains(species)) {
            player.sendMessage(I18n.getStrAndHeader("black-list"));
            return false;
        }
        if (PokemonInfo.getModule().getIVStoreValue(pokemonObj) > configuration.getInt("settings.maximum-ivs")) {
            player.sendMessage(I18n.getStrAndHeader("maximum-ivs"));
            return false;
        }
        if (!configuration.getBoolean("settings.color") && PokemonInfo.getModule().getPokemonCustomName(pokemonObj).contains("§")) {
            player.sendMessage(I18n.getStrAndHeader("color"));
            return false;
        }
        if (PokemonInfo.getModule().hasFlags(pokemonObj, configuration.getStringList("settings.flags").toArray(new String[0]))
                || PokemonInfo.getModule().isCancelled(pokemonObj)) {
            player.sendMessage(I18n.getStrAndHeader("denied"));
            return false;
        }
        PlayerConvertPokemonEvent event = new PlayerConvertPokemonEvent(player, pokemonObj);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (event.isNotify()) {
                player.sendMessage(I18n.getStrAndHeader("denied"));
            }
            return false;
        }
        PokemonInfo.getModule().retrieveAll(player.getUniqueId());
        PokemonInfo.getModule().setPartyPokemon(player.getUniqueId(), pokemonSlot, null);
        String uuid = UUID.randomUUID().toString();

        NBTItem spriteItem = new NBTItem(PokemonConvertView.getPokemonItem(pokemonObj, false));
        spriteItem.setString(CacheManager.getDataKey(), uuid);

        PokemonCache pokemonCache = new PokemonCache(uuid, pokemonObj);
        AbstractPersistenceDataImpl.getInstance().addPokemonCache(pokemonCache);

        player.getInventory().addItem(spriteItem.getItem());
        player.sendMessage(I18n.getStrAndHeader("convert"));
        return true;
    }

    public static String findTypeByCaptureList(String species, List<String> flags) {
        return Configuration.getInfoModuleConfig().getStringList("capture.list").stream()
                .filter((v) -> v.equals(species) || v.startsWith(species + ",") || flags.contains(v))
                .map((v) -> {
                    String[] split = species.split(",");
                    return split.length > 0 ? split[1] : split[0];
                })
                .findFirst()
                .orElse("");
    }
}
