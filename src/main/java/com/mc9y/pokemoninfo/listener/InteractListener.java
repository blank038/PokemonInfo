package com.mc9y.pokemoninfo.listener;

import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.data.PokeEgg;
import com.mc9y.pokemoninfo.i18n.I18n;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 * @since 2021-05-15
 */
public class InteractListener implements Listener {
    private final List<String> delays = new ArrayList<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            String playerName = e.getPlayer().getName();
            if (e.isCancelled() || delays.contains(playerName)) {
                return;
            }
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if (item == null || !"PIXELMON_PIXELMON_SPRITE".equals(item.getType().name()) || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                return;
            }
            net.minecraft.server.v1_12_R1.ItemStack nmsitem = CraftItemStack.asNMSCopy(item);
            if (!nmsitem.hasTag()) {
                return;
            }
            NBTTagCompound compound = nmsitem.getTag();
            assert compound != null;
            if (compound.hasKey("PokeEggUUID")) {
                String uuid = compound.getString("PokeEggUUID");
                PokeEgg pokeEgg = Main.getInstance().getDataInterface().get(uuid);
                // 增加玩家冷却
                delays.add(playerName);
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> delays.remove(playerName), Main.getInstance().getConfig().getInt("convert-delay"));
                // 判断是否无
                if (pokeEgg != null) {
                    e.setCancelled(true);
                    e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    if (Main.getInstance().getConfig().getBoolean("async")) {
                        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> run(e.getPlayer(), pokeEgg));
                    } else {
                        run(e.getPlayer(), pokeEgg);
                    }
                }
            }
        }
    }

    private void run(Player player, PokeEgg pokeEgg) {
        player.sendMessage(Main.getInstance().pec.prefix + I18n.inst().getOption("converting"));
        if (Main.getInstance().getDataInterface().remove(pokeEgg)) {
            PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
            pps.add(pokeEgg.getPokemon());
            player.sendMessage(Main.getInstance().pec.prefix + I18n.inst().getOption("convert_complete"));
        }
    }
}
