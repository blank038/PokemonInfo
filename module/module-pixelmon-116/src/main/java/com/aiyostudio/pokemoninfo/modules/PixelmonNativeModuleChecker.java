package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.PokemonInfo;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.aiyostudio.pokemoninfo.modules.listen.ForgeNativeListener;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.function.BooleanSupplier;

/**
 * @author Blank038
 */
public class PixelmonNativeModuleChecker implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        if (Pixelmon.getVersion().startsWith("9")) {
            PokemonInfo.setModule(new PixelmonNativeModuleImpl());
            Pixelmon.EVENT_BUS.addListener(EventPriority.NORMAL, true,
                    LegendarySpawnEvent.DoSpawn.class, ForgeNativeListener.LEGENDARY_SPAWN_CONSUMER);
            return true;
        }
        return false;
    }
}
