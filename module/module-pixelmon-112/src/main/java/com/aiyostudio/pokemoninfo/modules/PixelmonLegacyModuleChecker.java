package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.modules.listen.ForgeLegacyListener;
import com.aystudio.core.forge.ForgeInject;
import com.aystudio.core.pixelmon.PokemonAPI;
import com.aystudio.core.pixelmon.api.enums.PixelmonVersionEnum;
import org.bukkit.event.EventPriority;

import java.util.function.BooleanSupplier;

/**
 * @author Blank038
 */
public class PixelmonLegacyModuleChecker implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        if (PokemonAPI.getInstance().getPixelmonVersion() == PixelmonVersionEnum.v1_12_R1) {
            PokemonInfo.setModule(new PixelmonLegacyModuleImpl());
            ForgeInject.getForgeListenerHandler().registerListener(PokemonInfo.getInstance(), new ForgeLegacyListener(), EventPriority.NORMAL);
            return true;
        }
        return false;
    }
}
