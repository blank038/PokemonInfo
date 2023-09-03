package modules;

import com.aiyostudio.pokemoninfo.PokemonInfo;
import com.aiyostudio.pokemoninfo.cache.PokemonCache;
import com.aiyostudio.pokemoninfo.debug.DebugControl;
import com.aiyostudio.pokemoninfo.interfaces.IModule;
import com.aiyostudio.pokemoninfo.util.Base64Util;
import com.aystudio.core.bukkit.AyCore;
import com.aystudio.core.pixelmon.api.pokemon.PokemonUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import modules.listen.ForgeNativeListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraftforge.eventbus.api.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PixelmonNativeModuleImpl implements IModule<Pokemon> {

    @Override
    public void run() {
        if (Pixelmon.getVersion().startsWith("9")) {
            PokemonInfo.setModule(this);
            Pixelmon.EVENT_BUS.addListener(EventPriority.NORMAL, true,
                    LegendarySpawnEvent.DoSpawn.class, ForgeNativeListener.LEGENDARY_SPAWN_CONSUMER);
        }
    }

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
            CompoundNBT CompoundNBT = JsonToNBT.parseTag(Base64Util.decode(str));
            return PokemonFactory.create(CompoundNBT);
        } catch (CommandSyntaxException e) {
            DebugControl.log(Level.SEVERE, e.toString());
        }
        return null;
    }

    @Override
    public String pokemonToString(Pokemon pokemon) {
        CompoundNBT CompoundNBT = new CompoundNBT();
        pokemon.writeToNBT(CompoundNBT);
        return CompoundNBT.toString();
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
}
