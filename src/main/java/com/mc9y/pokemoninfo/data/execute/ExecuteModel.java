package com.mc9y.pokemoninfo.data.execute;

import java.sql.Connection;
import java.sql.Statement;

@FunctionalInterface
public interface ExecuteModel {

    void run(Connection connection, Statement statement);
}
