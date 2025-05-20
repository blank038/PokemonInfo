package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.debug.DebugControl;
import com.aiyostudio.pokemoninfo.internal.interfaces.IModule;
import com.aiyostudio.pokemoninfo.internal.util.TextUtil;
import com.aystudio.core.bukkit.AyCore;
import com.aystudio.core.pixelmon.PokemonAPI;
import com.aystudio.core.pixelmon.api.pokemon.PokemonUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * @author Blank038
 */
public class PixelmonLegacyModuleImpl implements IModule<Pokemon> {

    @Override
    public Pokemon fileToPokemon(File file) {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath()))) {
            NBTTagCompound nbt = CompressedStreamTools.read(dis);
            return Pixelmon.pokemonFactory.create(nbt);
        } catch (Exception e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    @Override
    public Pokemon stringToPokemon(String str) {
        try {
            NBTTagCompound nbtTagCompound = JsonToNBT.getTagFromJson(str);
            return Pixelmon.pokemonFactory.create(nbtTagCompound);
        } catch (NBTException e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    @Override
    public String pokemonToString(Pokemon pokemon) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        pokemon.writeToNBT(nbtTagCompound);
        return nbtTagCompound.toString();
    }

    @Override
    public boolean writePokemonToFile(PokemonCache pokemonCache, File file) {
        if (pokemonCache == null || pokemonCache.getPokemonData() == null) {
            return false;
        }
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(file.toPath()))) {
            NBTTagCompound nbt = new NBTTagCompound();
            ((Pokemon) pokemonCache.getPokemonData()).writeToNBT(nbt);
            CompressedStreamTools.write(nbt, dos);
            return true;
        } catch (IOException e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return false;
    }

    @Override
    public boolean isNull(UUID uuid, int pokemonSlot, boolean egg) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        if (storage == null || pokemonSlot < 0 || pokemonSlot >= 6) {
            return true;
        }
        Pokemon pokemon = storage.get(pokemonSlot);
        if (pokemon == null) {
            return true;
        }
        if (egg && pokemon.isEgg()) {
            return false;
        }
        return pokemon.isEgg();
    }

    @Override
    public boolean isShiny(Pokemon pokemon) {
        return pokemon != null && pokemon.isShiny();
    }

    @Override
    public Pokemon getPokemon(UUID uuid, int pokemonSlot) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        if (storage == null || pokemonSlot < 0 || pokemonSlot >= 6) {
            return null;
        }
        return storage.get(pokemonSlot);
    }

    @Override
    public String getPokemonTranslationName(Pokemon pokemonObj) {
        if (pokemonObj == null) {
            return "NULL";
        }
        return PokemonUtil.getPokemonName(pokemonObj.getSpecies());
    }

    @Override
    public String getSpecies(Pokemon pokemonObj) {
        return pokemonObj.getSpecies().name();
    }

    @Override
    public int getIVStoreValue(Pokemon pokemonObj) {
        return (int) Arrays.stream(pokemonObj.getIVs().getArray())
                .filter((s) -> s >= 31)
                .count();
    }

    @Override
    public String getPokemonCustomName(Pokemon pokemonObj) {
        if (pokemonObj.getDisplayName() != null) {
            return pokemonObj.getDisplayName();
        }
        return pokemonObj.getSpecies().name();
    }

    @Override
    public List<String> formatStats(Pokemon pokemonObj, List<String> stats) {
        if (pokemonObj == null) {
            return stats;
        }
        return this.getPokemonApi().getStatsHelper().format(pokemonObj, stats);
    }

    @Override
    public List<String> internalFormat(Pokemon pokemon, List<String> stats) {
        if (pokemon == null) {
            return stats;
        }
        String state = PokemonInfo.getInstance().getConfig().getString("custom-format.ivs.hyper-trained");
        if (state == null) {
            return stats;
        }
        boolean combatpower = Bukkit.getPluginManager().getPlugin("CombatPower") != null;
        boolean pokeStar = Bukkit.getPluginManager().getPlugin("PokeStar") != null;
        List<String> result = new ArrayList<>(stats);
        result.replaceAll((line) -> {
            String replacement = line;
            Matcher matcher = TextUtil.IVS_PATTERN.matcher(line);
            while (matcher.find()) {
                String group = matcher.group(), type = group.replace("%", "").split("_")[1];
                StatsType statsType = null;
                try {
                    statsType = StatsType.valueOf(type);
                } catch (Exception ignored) {
                }
                if (statsType == null) {
                    continue;
                }
                if (pokemon.getIVs().isHyperTrained(statsType)) {
                    replacement = replacement.replace(group, state.replace("%value%", group));
                }
            }
            if (combatpower) {
                com.mc9y.combatpower.api.CombatPowerAPI cpa = com.mc9y.combatpower.Main.getCombatPowerAPI();
                replacement = replacement.replace("%combat_power%", String.valueOf(cpa.getPokemonCombatPower(pokemon)));
            }
            if (pokeStar) {
                com.mc9y.pokestar.PokeStarAPI psa = com.mc9y.pokestar.Main.getPokeStarAPI();
                int star = psa.getPokemonStar(pokemon.getSpecies().name());
                replacement = replacement.replace("%star%", psa.getPokeShowName(star));
            }
            return replacement;
        });
        return this.formatStats(pokemon, result);
    }

    @Override
    public ItemStack getPokemonSpriteItem(Pokemon pokemonObj) {
        if (pokemonObj == null) {
            return null;
        }
        return this.getPokemonApi().getSpriteHelper().getSpriteItem(pokemonObj);
    }

    @Override
    public void setPartyPokemon(UUID uuid, int pokemonSlot, Pokemon pokemon) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        storage.set(pokemonSlot, pokemon);
    }

    @Override
    public void addPokemon(UUID uuid, Pokemon pokemon) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        storage.add(pokemon);
    }

    @Override
    public int getPartyPokemonCount(UUID uuid) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        return storage.countPokemon();
    }

    @Override
    public void retrieveAll(UUID uuid) {
        PlayerPartyStorage storage = Pixelmon.storageManager.getParty(uuid);
        storage.retrieveAll();
    }

    @Override
    public boolean isCancelled(Pokemon pokemon) {
        return false;
    }

    @Override
    public boolean hasFlags(Pokemon pokemon, String... flags) {
        return Arrays.stream(flags).anyMatch(pokemon::hasSpecFlag);
    }

    private PokemonAPI getPokemonApi() {
        try {
            return (PokemonAPI) AyCore.class.getMethod("getPokemonAPI").invoke(null);
        } catch (Exception e) {
            return PokemonAPI.getInstance();
        }
    }
}
