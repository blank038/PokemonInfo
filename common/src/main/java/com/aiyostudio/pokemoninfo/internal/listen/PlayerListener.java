package com.aiyostudio.pokemoninfo.internal.listen;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.manager.ActionCooldownManager;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Blank038
 */
public class PlayerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onRight(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return;
            }
            Player player = event.getPlayer();
            if (ActionCooldownManager.isCooldown("action", player.getName())) {
                return;
            }
            NBTItem nbtItem = new NBTItem(itemStack);
            if (nbtItem.hasTag("PokemonDataKey")) {
                event.setCancelled(true);
                ActionCooldownManager.setCooldown("action", player.getName(),
                        Configuration.getPokeEggModuleConfig().getInt("cooldown.action") * 1000L);
                String uuid = nbtItem.getString("PokemonDataKey");
                PokemonCache pokemonCache = AbstractPersistenceDataImpl.getInstance().getPokemonCache(uuid);
                if (AbstractPersistenceDataImpl.getInstance().removePokemonCache(uuid)) {
                    player.getInventory().setItemInMainHand(null);
                    PokemonInfo.getModule().addPokemon(player.getUniqueId(), pokemonCache.getPokemonData());
                    player.sendMessage(I18n.getStrAndHeader("convert"));
                }
            }
        }
    }
}
