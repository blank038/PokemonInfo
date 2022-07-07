package com.mc9y.pokemoninfo.data;

import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.i18n.I18n;
import com.mc9y.pokemoninfo.utils.Base64Util;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang.StringUtils;

import java.io.*;

public class PokeEgg {
    private final String uuid;
    private Pokemon pokemon;

    public PokeEgg(File f) {
        uuid = f.getName().replace(".pke", "");
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            NBTTagCompound nbt = CompressedStreamTools.read(dis);
            pokemon = Pixelmon.pokemonFactory.create(nbt);
            dis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Main.getInstance().getLogger().info(I18n.inst().getOption("loading_error")
                    .replace("%s", StringUtils.split(f.getName(), '.')[0]));
        }
    }

    public PokeEgg(File f, Pokemon pokemon) {
        uuid = f.getName().replace(".pke", "");
        write(f, pokemon);
        Main.getInstance().getDataInterface().add(this);
    }

    /**
     * MySQL 所用构建方法
     */
    public PokeEgg(String uuid, String nbt) {
        // 设置 UUID
        this.uuid = uuid;
        // 转换宝可梦数据
        try {
            NBTTagCompound nbtTagCompound = JsonToNBT.getTagFromJson(Base64Util.decode(nbt));
            pokemon = Pixelmon.pokemonFactory.create(nbtTagCompound);
        } catch (NBTException ignored) {
            Main.getInstance().getLogger().info(I18n.inst().getOption("loading_error")
                    .replace("%s", StringUtils.split(uuid, '.')[0]));
        }
    }

    public void write(File f, Pokemon pokemon) {
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
            NBTTagCompound nbt = new NBTTagCompound();
            pokemon.writeToNBT(nbt);
            CompressedStreamTools.write(nbt, dos);
            dos.flush();
            dos.close();
            this.pokemon = pokemon;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUUID() {
        return uuid;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }
}