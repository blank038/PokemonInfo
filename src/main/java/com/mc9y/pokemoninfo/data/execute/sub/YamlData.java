package com.mc9y.pokemoninfo.data.execute.sub;

import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.data.PokeEgg;
import com.mc9y.pokemoninfo.data.execute.DataInterface;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author Blank038
 */
public class YamlData extends DataInterface {
    private final HashMap<String, PokeEgg> eggs = new HashMap<>();

    public YamlData() {
        File data = new File(Main.getInstance().getDataFolder(), "PokeEggs");
        if (!data.exists()) {
            data.mkdir();
        }
        for (File f : Objects.requireNonNull(data.listFiles())) {
            add(new PokeEgg(f));
        }
    }

    @Override
    public PokeEgg get(String name) {
        return eggs.getOrDefault(name, null);
    }

    @Override
    public void add(PokeEgg pokeEgg) {
        eggs.put(pokeEgg.getUUID(), pokeEgg);
    }

    @Override
    public boolean remove(PokeEgg pokeEgg) {
        new File(Main.getInstance().getDataFolder() + "/PokeEggs/", pokeEgg.getUUID() + ".pke").delete();
        return eggs.remove(pokeEgg.getUUID()) != null;
    }
}