package com.mc9y.pokemoninfo.listener;

import com.mc9y.pokemonapi.api.event.ForgeEvent;
import com.mc9y.pokemoninfo.Main;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestListener implements Listener {
    private final List<EnumSpecies> enumSpecies = new ArrayList<>();

    public RequestListener() {
        this.enumSpecies.addAll(Arrays.asList(EnumSpecies.LEGENDARY_ENUMS));
    }

    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent e) {
        PlayerPartyStorage pps = Pixelmon.storageManager.getParty(e.getPlayer().getUniqueId());
        if (pps != null && BattleRegistry.getBattle(pps.getPlayer()) != null) {
            BattleRegistry.deRegisterBattle(BattleRegistry.getBattle(pps.getPlayer()));
        }
    }

    @EventHandler
    public void onCapture(ForgeEvent event) {
        if (event.getForgeEvent() instanceof CaptureEvent.SuccessfulCapture) {
            CaptureEvent.SuccessfulCapture e = (CaptureEvent.SuccessfulCapture) event.getForgeEvent();
            if (Main.getInstance().pokemonInfoCommand.inCaptureList(e.getPokemon().getSpecies().name())) {
                Main.getInstance().pokemonInfoCommand.sendCaptureText(Bukkit.getPlayer(e.player.getDisplayNameString()), e.getPokemon().getStoragePokemonData(), e.getPokemon().getSpecies().name());
            } else if (Main.getInstance().pokemonInfoCommand.inCaptureList("legend") && enumSpecies.contains(e.getPokemon().getSpecies())) {
                Main.getInstance().pokemonInfoCommand.sendCaptureText(Bukkit.getPlayer(e.player.getDisplayNameString()), e.getPokemon().getStoragePokemonData(), "legend");
            }
        } else if (event.getForgeEvent() instanceof LegendarySpawnEvent.DoSpawn) {
            LegendarySpawnEvent.DoSpawn e = (LegendarySpawnEvent.DoSpawn) event.getForgeEvent();
            if (e.action instanceof SpawnActionPokemon) {
                Main.getInstance().pokemonInfoCommand.sendLegendSpawnText(e.action.getOrCreateEntity());
            }
        } else if (event.getForgeEvent() instanceof SpawnEvent) {
            SpawnEvent spawnEvent = (SpawnEvent) event.getForgeEvent();
            if (spawnEvent.action instanceof SpawnActionPokemon) {
                EntityPixelmon entityPixelmon = (EntityPixelmon) spawnEvent.action.getOrCreateEntity();
                if (!entityPixelmon.isBossPokemon() && !enumSpecies.contains(entityPixelmon.getSpecies()) &&
                        Main.getInstance().pokemonInfoCommand.inSpawnList(entityPixelmon.getSpecies().name())) {
                    Main.getInstance().pokemonInfoCommand.sendPokemonSpawnText(entityPixelmon);
                }
            }

        }
    }
}