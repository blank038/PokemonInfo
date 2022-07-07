package com.mc9y.pokemoninfo.data.execute;

import com.mc9y.pokemoninfo.data.PokeEgg;

import java.sql.Connection;
import java.sql.Statement;

public abstract class DataInterface {

    public abstract PokeEgg get(String key);

    public void connect(ExecuteModel executeModel) {
    }

    public void close(Connection connection, Statement statement) {
    }

    public void importData() {
    }

    public abstract void add(PokeEgg pokeEgg);

    public abstract boolean remove(PokeEgg pokeEgg);
}