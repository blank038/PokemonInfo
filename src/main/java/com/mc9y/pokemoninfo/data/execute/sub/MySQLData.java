package com.mc9y.pokemoninfo.data.execute.sub;

import java.io.File;
import java.sql.*;

import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.data.PokeEgg;
import com.mc9y.pokemoninfo.data.execute.DataInterface;
import com.mc9y.pokemoninfo.data.execute.ExecuteModel;
import com.mc9y.pokemoninfo.utils.Base64Util;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MySQLData extends DataInterface {
    private final String user = Main.getInstance().getConfig().getString("save-option.user"), url = Main.getInstance().getConfig().getString("save-option.url"),
            password = Main.getInstance().getConfig().getString("save-option.password");

    public MySQLData() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        createTable();
    }

    public void createTable() {
        connect((connection, statement) -> {
            try {
                String sql = "CREATE TABLE IF NOT EXISTS pokemoninfo (uuid VARCHAR(50) NOT NULL, data TEXT, PRIMARY KEY ( uuid ))";
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public PokeEgg get(String key) {
        AtomicReference<String> jsonObject = new AtomicReference<>();
        connect((connection, statement) -> {
            String sql = String.format("SELECT data FROM pokemoninfo WHERE uuid='%s'", key);
            try {
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    String data = resultSet.getString("data");
                    if (data != null) {
                        jsonObject.set(data);
                        break;
                    }
                }
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return jsonObject.get() == null ? null : new PokeEgg(key, jsonObject.get());
    }

    @Override
    public void connect(ExecuteModel executeModel) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            executeModel.run(connection, statement);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(connection, statement);
        }
    }

    @Override
    public void close(Connection connection, Statement statement) {
        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importData() {
        // 上传本地玩家数据
        File dataFiles = new File(Main.getInstance().getDataFolder(), "PokeEggs");
        connect((connection, statement) -> {
            for (File i : Objects.requireNonNull(dataFiles.listFiles())) {
                PokeEgg pokeEgg = new PokeEgg(i);
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                pokeEgg.getPokemon().writeToNBT(nbtTagCompound);
                String sql = String.format("INSERT INTO pokemoninfo (uuid,data) VALUES ('%s','%s')", pokeEgg.getUUID(), Base64Util.encode(nbtTagCompound.toString()));
                try {
                    statement.executeUpdate(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void add(PokeEgg pokeEgg) {
        if (pokeEgg == null) {
            return;
        }
        connect((connection, statement) -> {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            pokeEgg.getPokemon().writeToNBT(nbtTagCompound);
            String sql = String.format("INSERT INTO pokemoninfo (uuid,data) VALUES ('%s','%s')", pokeEgg.getUUID(), Base64Util.encode(nbtTagCompound.toString()));
            try {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean remove(PokeEgg pokeEgg) {
        if (pokeEgg == null) {
            return false;
        }
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        connect((connection, statement) -> {
            try {
                if (this.get(pokeEgg.getUUID()) != null) {
                    atomicBoolean.set(true);
                    String sql = String.format("DELETE FROM pokemoninfo WHERE uuid='%s'", pokeEgg.getUUID());
                    statement.executeUpdate(sql);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        return atomicBoolean.get();
    }
}
