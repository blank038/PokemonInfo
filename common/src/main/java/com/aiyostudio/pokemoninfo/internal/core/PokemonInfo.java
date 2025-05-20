package com.aiyostudio.pokemoninfo.internal.core;

import com.aiyostudio.pokemoninfo.internal.command.PokeEggCommand;
import com.aiyostudio.pokemoninfo.internal.command.PokemonInfoCommand;
import com.aiyostudio.pokemoninfo.internal.config.Configuration;
import com.aiyostudio.pokemoninfo.internal.debug.DebugControl;
import com.aiyostudio.pokemoninfo.internal.dao.AbstractPersistenceDataImpl;
import com.aiyostudio.pokemoninfo.internal.interfaces.IModule;
import com.aiyostudio.pokemoninfo.internal.listen.PlayerListener;
import com.aiyostudio.pokemoninfo.internal.manager.ActionCooldownManager;
import com.aiyostudio.pokemoninfo.internal.metrics.Metrics;
import com.aystudio.core.bukkit.plugin.AyPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.function.BooleanSupplier;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PokemonInfo extends AyPlugin {
    private static final String[] MODULES = {
            "com.aiyostudio.pokemoninfo.modules.PixelmonLegacyModuleChecker",
            "com.aiyostudio.pokemoninfo.modules.PixelmonNativeModuleChecker"
    };
    @Getter
    private static PokemonInfo instance;
    @Getter
    @Setter
    private static IModule module;

    @Override
    public void onEnable() {
        instance = this;
        // initialize configuration
        Configuration.init(this);
        // initialize all modules
        this.initializeModules();
        ActionCooldownManager.init();
        AbstractPersistenceDataImpl.of(this.getConfig().getConfigurationSection("save-option"));

        this.getCommand("pi").setExecutor(new PokemonInfoCommand());
        this.getCommand("pokeegg").setExecutor(new PokeEggCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // metrics stats
        new Metrics(this);
    }

    private void initializeModules() {
        for (String moduleClass : PokemonInfo.MODULES) {
            try {
                Class<?> aClass = Class.forName(moduleClass);
                BooleanSupplier supplier = (BooleanSupplier) aClass.newInstance();
                supplier.getAsBoolean();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                DebugControl.debug(Level.SEVERE, e, "Failed to load module.");
            }
        }
    }
}
