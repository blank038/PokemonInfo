package com.aiyostudio.pokemoninfo.modules;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.modules.listen.ForgeLegacyListener;
import com.aystudio.core.forge.ForgeInject;
import org.bukkit.event.EventPriority;

import java.util.function.BooleanSupplier;

/**
 * @author Blank038
 */
public class PixelmonLegacyModuleChecker implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        if (com.pixelmonmod.pixelmon.Pixelmon.getVersion().startsWith("8")) {
            PokemonInfo.setModule(new PixelmonLegacyModuleImpl());
            ForgeInject.getInstance().getForgeListener()
                    .registerListener(PokemonInfo.getInstance(), new ForgeLegacyListener(), EventPriority.NORMAL);
            return true;
        }
        return false;
    }
}
