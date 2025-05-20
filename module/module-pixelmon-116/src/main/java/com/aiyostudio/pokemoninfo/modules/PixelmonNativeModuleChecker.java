package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aystudio.core.pixelmon.PokemonAPI;
import com.aystudio.core.pixelmon.api.enums.PixelmonVersionEnum;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.LegendarySpawnEvent;
import com.aiyostudio.pokemoninfo.modules.listen.ForgeNativeListener;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @author Blank038
 */
public class PixelmonNativeModuleChecker implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        if (PokemonAPI.getInstance().getPixelmonVersion() == PixelmonVersionEnum.v1_16_R3) {
            PokemonInfo.setModule(new PixelmonNativeModuleImpl());
            Map<Class<Event>, Consumer<Event>> events = new HashMap() {
                {
                    put(LegendarySpawnEvent.DoSpawn.class, ForgeNativeListener.LEGENDARY_SPAWN_CONSUMER);
                    put(CaptureEvent.SuccessfulCapture.class, ForgeNativeListener.CAPTURE_CONSUMER);
                }
            };
            events.forEach((k, v) -> Pixelmon.EVENT_BUS.addListener(EventPriority.NORMAL, true, k, v));
            return true;
        }
        return false;
    }
}
