package com.mc9y.pokemoninfo;

import com.mc9y.pokemoninfo.command.PokeEggCommand;
import com.mc9y.pokemoninfo.command.PokemonInfoCommand;
import com.mc9y.pokemoninfo.data.execute.DataInterface;
import com.mc9y.pokemoninfo.data.execute.sub.MySQLData;
import com.mc9y.pokemoninfo.data.execute.sub.YamlData;
import com.mc9y.pokemoninfo.i18n.I18n;
import com.mc9y.pokemoninfo.listener.InteractListener;
import com.mc9y.pokemoninfo.listener.RequestListener;
import com.mc9y.pokemoninfo.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Blank038
 */
public class Main extends JavaPlugin
        implements Listener {
    private static Main main;
    private DataInterface dataInterface;
    public PokeEggCommand pec;
    public FileConfiguration guiData;
    public PokemonInfoCommand pokemonInfoCommand;

    public DataInterface getDataInterface() {
        return this.dataInterface;
    }

    @Override
    public void onEnable() {
        main = this;
        loadData(true);
        // 初始化接口
        if (getConfig().getString("save-option.type").equalsIgnoreCase("mysql")) {
            this.dataInterface = new MySQLData();
        } else {
            this.dataInterface = new YamlData();
        }
        // 注册命令及监听器
        this.pec = new PokeEggCommand("pokeegg");
        this.pokemonInfoCommand = new PokemonInfoCommand("pi", this);
        this.getCommand("pokeegg").setExecutor(pec);
        this.getCommand("pi").setExecutor(pokemonInfoCommand);
        Bukkit.getPluginManager().registerEvents(new RequestListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        new Metrics(this);
        this.reloadPluginData();
    }

    public void loadData(boolean start) {
        this.saveDefaultConfig();
        File gui = new File(getDataFolder(), "gui.yml");
        if (!gui.exists()) {
            saveResource("gui.yml", true);
        }
        this.reloadConfig();
        guiData = YamlConfiguration.loadConfiguration(gui);
        if (!start) {
            this.reloadPluginData();
        }
        new I18n(this.getConfig().getString("language", "zh_CN"));
    }

    public void reloadPluginData() {
        this.pec.reloadConfig();
        this.pokemonInfoCommand.reloadConfig();
        this.pokemonInfoCommand.reloadCaptureList();
    }

    public static Main getInstance() {
        return main;
    }
}