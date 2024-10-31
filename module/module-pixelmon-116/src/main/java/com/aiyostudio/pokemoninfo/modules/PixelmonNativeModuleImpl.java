package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.internal.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.debug.DebugControl;
import com.aiyostudio.pokemoninfo.internal.interfaces.IModule;
import com.aiyostudio.pokemoninfo.internal.util.TextUtil;
import com.aystudio.core.bukkit.AyCore;
import com.aystudio.core.pixelmon.api.pokemon.PokemonUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;

/**
 * @author Blank038
 */
public class PixelmonNativeModuleImpl implements IModule<Pokemon> {

    @Override
    public Pokemon fileToPokemon(File file) {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file.toPath()))) {
            CompoundNBT nbt = CompressedStreamTools.read(dis);
            return PokemonFactory.create(nbt);
        } catch (Exception e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    @Override
    public Pokemon stringToPokemon(String str) {
        try {
            CompoundNBT compoundNBT = JsonToNBT.parseTag(str);
            return PokemonFactory.create(compoundNBT);
        } catch (CommandSyntaxException e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    @Override
    public String pokemonToString(Pokemon pokemon) {
        CompoundNBT compoundNBT = new CompoundNBT();
        pokemon.writeToNBT(compoundNBT);
        return compoundNBT.toString();
    }

    @Override
    public boolean writePokemonToFile(PokemonCache pokemonCache, File file) {
        if (pokemonCache == null || pokemonCache.getPokemonData() == null) {
            return false;
        }
        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(file.toPath()))) {
            CompoundNBT nbt = new CompoundNBT();
            ((Pokemon) pokemonCache.getPokemonData()).writeToNBT(nbt);
            CompressedStreamTools.write(nbt, dos);
            return true;
        } catch (IOException e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return false;
    }

    @Override
    public boolean isNullOrEgg(UUID uuid, int pokemonSlot) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
        if (storage == null || pokemonSlot < 0 || pokemonSlot >= 6) {
            return true;
        }
        Pokemon pokemon = storage.get(pokemonSlot);
        return pokemon == null || pokemon.isEgg();
    }

    @Override
    public boolean isShiny(Pokemon pokemon) {
        return pokemon != null && pokemon.isShiny();
    }

    @Override
    public Pokemon getPokemon(UUID uuid, int pokemonSlot) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
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
        return pokemonObj.getSpecies().getName();
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
        return pokemonObj.getSpecies().getName();
    }

    @Override
    public List<String> formatStats(Pokemon pokemonObj, List<String> stats) {
        if (pokemonObj == null) {
            return stats;
        }
        return AyCore.getPokemonAPI().getStatsHelper().format(pokemonObj, stats);
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
        Map<String, BattleStatsType> battleStatsTypeMap = new HashMap<>();
        battleStatsTypeMap.put("HP", BattleStatsType.HP);
        battleStatsTypeMap.put("Speed", BattleStatsType.SPEED);
        battleStatsTypeMap.put("Attack", BattleStatsType.ATTACK);
        battleStatsTypeMap.put("Defence", BattleStatsType.DEFENSE);
        battleStatsTypeMap.put("SpecialAttack", BattleStatsType.SPECIAL_ATTACK);
        battleStatsTypeMap.put("SpecialDefence", BattleStatsType.SPECIAL_DEFENSE);
        List<String> result = new ArrayList<>(stats);
        result.replaceAll((line) -> {
            String replacement = line;
            Matcher matcher = TextUtil.IVS_PATTERN.matcher(line);
            while (matcher.find()) {
                String group = matcher.group(), type = group.replace("%", "").split("_")[1];
                BattleStatsType battleStatsType = battleStatsTypeMap.get(type);
                if (battleStatsType == null) {
                    continue;
                }
                if (pokemon.getIVs().isHyperTrained(battleStatsType)) {
                    replacement = replacement.replace(group, state.replace("%value%", group));
                }
            }
            if (combatpower) {
                com.mc9y.combatpower.api.CombatPowerAPI cpa = com.mc9y.combatpower.Main.getCombatPowerAPI();
                replacement = replacement.replace("%combat_power%", String.valueOf(cpa.getPokemonCombatPower(pokemon)));
            }
            if (pokeStar) {
                com.mc9y.pokestar.PokeStarAPI psa = com.mc9y.pokestar.Main.getPokeStarAPI();
                int star = psa.getPokemonStar(pokemon.getSpecies().getName());
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
        return AyCore.getPokemonAPI().getSpriteHelper().getSpriteItem(pokemonObj);
    }

    @Override
    public void setPartyPokemon(UUID uuid, int pokemonSlot, Pokemon pokemon) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
        storage.set(pokemonSlot, pokemon);
    }

    @Override
    public void addPokemon(UUID uuid, Pokemon pokemon) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
        storage.add(pokemon);
    }

    @Override
    public int getPartyPokemonCount(UUID uuid) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
        return storage.countPokemon();
    }

    @Override
    public void retrieveAll(UUID uuid) {
        PlayerPartyStorage storage = StorageProxy.getParty(uuid);
        storage.retrieveAll("Method retrieveAll is called by PokemonInfo");
    }

    @Override
    public boolean isCancelled(Pokemon pokemon) {
        return false;
    }

    @Override
    public boolean hasFlags(Pokemon pokemon, String... flags) {
        return Arrays.stream(flags).anyMatch(pokemon::hasFlag);
    }
}
