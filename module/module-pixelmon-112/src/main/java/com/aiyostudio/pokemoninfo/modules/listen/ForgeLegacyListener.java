package com.aiyostudio.pokemoninfo.modules.listen;

import com.aiyostudio.pokemoninfo.api.PokemonInfoApi;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.message.CustomMessage;
import com.aiyostudio.pokemoninfo.internal.util.TextUtil;
import com.aystudio.core.forge.IForgeListenHandler;
import com.aystudio.core.pixelmon.api.pokemon.PokemonUtil;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Blank038
 */
public class ForgeLegacyListener implements Listener {

    @IForgeListenHandler.SubscribeEvent
    public void onForge(Event event) {
        if (event instanceof LegendarySpawnEvent.DoSpawn) {
            LegendarySpawnEvent.DoSpawn e = (LegendarySpawnEvent.DoSpawn) event;
            if (e.action instanceof SpawnActionPokemon && Configuration.getInfoModuleConfig().getBoolean("legendary.enable")) {
                Bukkit.getScheduler().runTaskLater(PokemonInfo.getInstance(), () -> {
                    FileConfiguration configuration = Configuration.getInfoModuleConfig();
                    EntityPixelmon entityPixelmon = e.action.getOrCreateEntity();
                    Entity entity = Bukkit.getEntity(entityPixelmon.getUniqueID());
                    Location location = entity.getLocation();

                    String nearbyPlayer = this.getNearbyPlayer(entity, configuration.getInt("legendary.distance"));
                    Pokemon pokemon = entityPixelmon.getPokemonData();
                    String message = configuration.getString("legendary.text")
                            .replace("%x%", String.valueOf(location.getBlockX()))
                            .replace("%y%", String.valueOf(location.getBlockY()))
                            .replace("%z%", String.valueOf(location.getBlockZ()))
                            .replace("%world%", location.getWorld().getName())
                            .replace("%player%", nearbyPlayer);
                    String pokemonName = configuration.getString("legendary.pokemon-name")
                            .replace("%pokemon_name%", PokemonInfo.getModule().getPokemonTranslationName(pokemon));

                    List<String> stats = new ArrayList<>(), formatStats = PokemonInfo.getModule().formatStats(pokemon, configuration.getStringList("legendary.hover"));
                    for (int i = 0; i < formatStats.size(); i++) {
                        if (i + 1 == formatStats.size()) {
                            stats.add(formatStats.get(i));
                            break;
                        }
                        stats.add(formatStats.get(i) + "\n");
                    }

                    CustomMessage customMessage = new CustomMessage.Build()
                            .setMessage(message)
                            .setPokemonName(TextUtil.formatHexColor(pokemonName))
                            .setHolverLines(stats)
                            .build();
                    customMessage.broadcast();
                }, 10L);
            }
        } else if (event instanceof CaptureEvent.SuccessfulCapture) {
            CaptureEvent.SuccessfulCapture evt = (CaptureEvent.SuccessfulCapture) event;
            Pokemon pokemon = evt.getPokemon().getPokemonData();
            List<String> species = Configuration.getInfoModuleConfig().getStringList("capture.list");
            if (species.contains(pokemon.getSpecies().name())) {
                FileConfiguration configuration = Configuration.getInfoModuleConfig();
                String playerName = evt.player.getName();
                String pokemonName = PokemonUtil.getPokemonName(pokemon.getSpecies());
                List<String> hoverTexts = configuration.getStringList("capture.hover");
                List<String> stats = new ArrayList<>(), formatStats = PokemonInfo.getModule().formatStats(pokemon, hoverTexts);
                for (int i = 0; i < formatStats.size(); i++) {
                    if (i + 1 == formatStats.size()) {
                        stats.add(formatStats.get(i));
                        break;
                    }
                    stats.add(formatStats.get(i) + "\n");
                }
                List<String> flags = new ArrayList<>();
                if (pokemon.isLegendary()) {
                    flags.add("legendary");
                }
                if (pokemon.getSpecies().isUltraBeast()) {
                    flags.add("ultrabeast");
                }
                String message = configuration.getString("capture.text")
                        .replace("%player%", playerName)
                        .replace("%type%", PokemonInfoApi.findAliasByCaptureList(pokemon.getSpecies().name(), pokemonName, flags));
                CustomMessage customMessage = new CustomMessage.Build()
                        .setMessage(message)
                        .setPokemonName(TextUtil.formatHexColor(pokemonName))
                        .setHolverLines(stats)
                        .build();
                customMessage.broadcast();
            }
        }
    }

    private String getNearbyPlayer(Entity entity, int distance) {
        String player = Configuration.getInfoModuleConfig().getString("legendary.none");
        for (Player p : entity.getWorld().getPlayers()) {
            if (p.getLocation().distance(entity.getLocation()) <= distance) {
                player = p.getName();
                break;
            }
        }
        return player;
    }
}
