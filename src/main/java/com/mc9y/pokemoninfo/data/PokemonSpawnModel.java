package com.mc9y.pokemoninfo.data;

import com.mc9y.blank038api.Blank038API;
import com.mc9y.pokemonapi.api.pokemon.PokemonUtil;
import com.mc9y.pokemoninfo.Main;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class PokemonSpawnModel {
    private final int distance;

    public PokemonSpawnModel(ConfigurationSection section, EntityPixelmon ep, String type) {
        this.distance = section.getInt("distance");
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            int entityId = ep.getEntityId();
            LivingEntity entity = getPokemon(entityId);
            if (entity == null) {
                return;
            }
            String lp = getDistancePlayer(entity);
            Location loc = entity.getLocation().clone();
            String[] fg = formatLegendarySpawnMessage(loc, section.getString("text").replace("%type%", type), lp).split("%pokemon%");
            TextComponent header = new TextComponent(fg[0]);
            TextComponent pokemon = new TextComponent(PokemonUtil.getPokemonName(ep.getSpecies()));
            List<String> info = section.getStringList("hover");
            TextComponent[] showTexts = new TextComponent[info.size()];
            for (int i = 0; i < info.size(); i++) {
                showTexts[i] = new TextComponent(Blank038API.getPokemonAPI().getStatsHelper().format(ep.getStoragePokemonData(), info.get(i))
                        + ((i + 1) == info.size() ? "" : "\n"));
            }
            pokemon.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, showTexts));
            TextComponent footer = new TextComponent();
            if (fg.length > 1) {
                footer = new TextComponent(fg[1]);
            }
            header.addExtra(pokemon);
            header.addExtra(footer);
            for (Player i : Bukkit.getOnlinePlayers()) {
                i.spigot().sendMessage(header);
            }
        }, 10L);
    }

    public String getDistancePlayer(LivingEntity le) {
        String player = "无";
        for (Entity i : le.getNearbyEntities(distance, distance, distance)) {
            if (i instanceof Player) {
                player = i.getName();
                break;
            }
        }
        return player;
    }

    public String formatLegendarySpawnMessage(Location le, String text, String player) {
        return text.replace("&", "§").replace("%world%", le.getWorld().getName()).replace("%x%", le.getBlockX() + "")
                .replace("%y%", le.getBlockY() + "").replace("%z%", le.getBlockZ() + "")
                .replace("%player%", player);
    }

    public LivingEntity getPokemon(int entityid) {
        LivingEntity le = null;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity && entity.getEntityId() == entityid) {
                    le = (LivingEntity) entity;
                    break;
                }
            }
        }
        return le;
    }
}