package modules.listen;

import com.aiyostudio.pokemoninfo.PokemonInfo;
import com.aiyostudio.pokemoninfo.config.Configuration;
import com.aiyostudio.pokemoninfo.message.CustomMessage;
import com.aiyostudio.pokemoninfo.util.TextUtil;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Blank038
 */
public class ForgeNativeListener implements Listener {
    public static final Consumer<LegendarySpawnEvent.DoSpawn> LEGENDARY_SPAWN_CONSUMER = (e) -> {
        if (e.action instanceof SpawnActionPokemon && Configuration.getPIModuleConfig().getBoolean("legendary.enable")) {
            Bukkit.getScheduler().runTaskLater(PokemonInfo.getInstance(), () -> {
                FileConfiguration configuration = Configuration.getPIModuleConfig();
                PixelmonEntity entityPixelmon = e.action.getOrCreateEntity();
                Entity entity = Bukkit.getEntity(entityPixelmon.getUUID());
                Location location = entity.getLocation();

                String nearbyPlayer = ForgeNativeListener.getNearbyPlayer(entity, configuration.getInt("legendary.distance"));
                Pokemon pokemon = entityPixelmon.getPokemon();
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
    };

    private static String getNearbyPlayer(Entity entity, int distance) {
        String player = Configuration.getPIModuleConfig().getString("legendary.none");
        for (Player p : entity.getWorld().getPlayers()) {
            if (p.getLocation().distance(entity.getLocation()) <= distance) {
                player = p.getName();
                break;
            }
        }
        return player;
    }
}
