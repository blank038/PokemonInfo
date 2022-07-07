package com.mc9y.pokemoninfo.command;

import com.mc9y.blank038api.Blank038API;
import com.mc9y.blank038api.util.inventory.GuiModel;
import com.mc9y.pokemonapi.api.pokemon.PokemonUtil;
import com.mc9y.pokemoninfo.Main;
import com.mc9y.pokemoninfo.data.PokemonSpawnModel;
import com.mc9y.pokemoninfo.i18n.I18n;
import com.mc9y.pokemoninfo.utils.MessageUtil;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PokemonInfoCommand extends BaseCommand
        implements CommandExecutor {
    private final Main main;
    private final HashMap<String, String> captureList = new HashMap<>();
    private final List<String> delay = new ArrayList<>();
    private final HashMap<String, String> spawnTypeList = new HashMap<>();

    public PokemonInfoCommand(String type, Main main) {
        super(type);
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                openGUI((Player) sender);
            }
        } else if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("pokemoninfo.admin")) {
            main.loadData(false);
            sender.sendMessage(prefix + I18n.inst().getOption("reload"));
        } else if ("show".equalsIgnoreCase(args[0]) && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                sender.sendMessage(prefix + I18n.inst().getOption("wrong_slot"));
                return true;
            }
            if (delay.contains(sender.getName())) {
                sender.sendMessage(prefix + getConfig().getString("message.in-delay").replace("&", "§"));
                return true;
            }
            int slot;
            try {
                slot = Integer.parseInt(args[1]);
            } catch (Exception e) {
                sender.sendMessage(prefix + I18n.inst().getOption("wrong_slot"));
                return true;
            }
            slot -= 1;
            PlayerPartyStorage playerPartyStorage = Pixelmon.storageManager.getParty(player.getUniqueId());
            Pokemon pokemon = playerPartyStorage.get(slot);
            if (pokemon == null || pokemon.isEgg()) {
                sender.sendMessage(prefix + I18n.inst().getOption("wrong_pokemon"));
                return true;
            }
            showPokemon((Player) sender, slot);
        } else if ("import".equalsIgnoreCase(args[0]) && sender.hasPermission("pokemoninfo.admin")) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                sender.sendMessage(prefix + I18n.inst().getOption("import_start"));
                main.getDataInterface().importData();
                sender.sendMessage(prefix + I18n.inst().getOption("import_complete"));
            });
        }
        return false;
    }

    public void openGUI(Player p) {
        GuiModel guiModel = new GuiModel(main.guiData.getString("title").replace("&", "§"), main.guiData.getInt("size"));
        guiModel.registerListener(Main.getInstance());
        guiModel.setCloseRemove(true);
        HashMap<Integer, ItemStack> itemMap = new HashMap<>();
        if (main.guiData.getKeys(false).contains("items")) {
            // 玩家无精灵展示物品
            ItemStack br = new ItemStack(Material.valueOf(main.getConfig().getString("Fail.type")), 1);
            ItemMeta brim = br.getItemMeta();
            brim.setDisplayName(main.getConfig().getString("Fail.name").replace("&", "§"));
            br.setItemMeta(brim);
            // 开始遍历所有节点
            for (String key : main.guiData.getConfigurationSection("items").getKeys(false)) {
                ItemStack itemStack;
                if (main.guiData.getInt("items." + key + ".pokemon") > 0) {
                    itemStack = getPokemonShowItem(main.guiData.getInt("items." + key + ".pokemon") - 1, p, false);
                    if (itemStack == null) {
                        itemStack = br.clone();
                    }
                } else {
                    itemStack = new ItemStack(Material.valueOf(main.guiData.getString("items." + key + ".type")), 1,
                            (short) main.guiData.getInt("items." + key + ".data"));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(main.guiData.getString("items." + key + ".name").replace("&", "§"));
                    List<String> itemLore = new ArrayList<>();
                    for (String lore : main.guiData.getStringList("items." + key + ".lore")) {
                        itemLore.add(lore.replace("&", "§"));
                    }
                    itemMeta.setLore(itemLore);
                    itemStack.setItemMeta(itemMeta);
                }
                // 判断是多槽位还是单槽位并添加物品
                List<Integer> slots = main.guiData.getIntegerList("items." + key + ".slots");
                if (slots != null && slots.size() > 0) {
                    for (int slot : slots) {
                        itemMap.put(slot, itemStack);
                    }
                } else {
                    itemMap.put(main.guiData.getInt("items." + key + ".slot"), itemStack);
                }
            }
        }
        guiModel.setItem(itemMap);
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                ItemStack itemStack = e.getCurrentItem();
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    return;
                }
                if (main.guiData.getBoolean("egg")) {
                    net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
                    NBTTagCompound nbtTagCompound = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
                    assert nbtTagCompound != null;
                    if (nbtTagCompound.hasKey("pokemon")) {
                        Player player = (Player) e.getWhoClicked();
                        int pokemonSlot = nbtTagCompound.getInt("pokemon");
                        if (pokemonSlot >= 0 && pokemonSlot < 6) {
                            if (e.getClick().isLeftClick()) {
                                if (delay.contains(player.getName())) {
                                    player.sendMessage(prefix + getConfig().getString("message.in-delay").replace("&", "§"));
                                    return;
                                }
                                showPokemon(player, pokemonSlot);
                            } else if (e.getClick().isRightClick()) {
                                openPokeEggInventory(player, pokemonSlot);
                            }
                        }
                    }
                }
            }
        });
        guiModel.openInventory(p);
    }

    public void openPokeEggInventory(Player player, int slot) {
        PlayerPartyStorage playerPartyStorage = Pixelmon.storageManager.getParty(player.getUniqueId());
        Pokemon pokemon = playerPartyStorage.get(slot);
        if (pokemon == null || pokemon.isEgg()) {
            return;
        }
        GuiModel guiModel = new GuiModel(main.guiData.getString("eggTitle").replace("%pokemon%",
                PokemonUtil.getPokemonName(pokemon.getSpecies())), 27);
        guiModel.registerListener(Main.getInstance());
        guiModel.setCloseRemove(true);
        // 设置物品
        ItemStack success = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta successMeta = success.getItemMeta();
        successMeta.setDisplayName(I18n.inst().getOption("confirm_button"));
        success.setItemMeta(successMeta);
        guiModel.setItem(10, success);

        ItemStack deny = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.setDisplayName(I18n.inst().getOption("cancel_button"));
        deny.setItemMeta(denyMeta);
        guiModel.setItem(16, deny);
        guiModel.setItem(13, getPokemonShowItem(slot, player, true));
        guiModel.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() == e.getInventory()) {
                Player clicker = (Player) e.getWhoClicked();
                switch (e.getSlot()) {
                    case 10:
                        main.pec.convertPokemonToPhoto(clicker, new String[]{"", String.valueOf(slot + 1)});
                        openGUI(clicker);
                        break;
                    case 16:
                        openGUI(clicker);
                        break;
                    default:
                        break;
                }
            }
        });
        guiModel.openInventory(player);
    }

    public void showPokemon(Player player, int slot) {
        PlayerPartyStorage playerPartyStorage = Pixelmon.storageManager.getParty(player.getUniqueId());
        Pokemon pokemon = playerPartyStorage.get(slot);
        if (pokemon == null || pokemon.isEgg()) {
            return;
        }
        delay.add(player.getName());
        Bukkit.getScheduler().runTaskLaterAsynchronously(main, () -> delay.remove(player.getName()), 20L * getConfig().getInt("delay"));
        String[] split = getConfig().getString("show.text").replace("%player%", player.getName())
                .replace("&", "§").split("%pokemon%");
        TextComponent header = new TextComponent(split[0]);
        TextComponent pt = new TextComponent(PokemonUtil.getPokemonName(pokemon.getSpecies()));
        List<String> texts = getConfig().getStringList("show.hover");
        TextComponent[] showTexts = new TextComponent[texts.size()];
        for (int i = 0; i < texts.size(); i++) {
            showTexts[i] = new TextComponent(MessageUtil.formatText(pokemon,
                    Blank038API.getPokemonAPI().getStatsHelper().format(pokemon, texts.get(i))) + ((i + 1) == texts.size() ? "" : "\n"));
        }
        pt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, showTexts));
        TextComponent end = new TextComponent();
        if (split.length > 1) {
            end = new TextComponent(split[1]);
        }
        header.addExtra(pt);
        header.addExtra(end);
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.spigot().sendMessage(header);
        }
    }

    public ItemStack getPokemonShowItem(int slot, Player player, boolean info) {
        PlayerPartyStorage pps = Pixelmon.storageManager.getParty(player.getUniqueId());
        if (slot < 0 || slot >= 6) {
            return null;
        }
        Pokemon pokemon = pps.get(slot);
        if (pokemon == null) {
            return null;
        }
        ItemStack item = Blank038API.getPokemonAPI().getSpriteHelper().getSpriteItem(pokemon);
        List<String> lore = Blank038API.getPokemonAPI().getStatsHelper()
                .format(pokemon, main.getConfig().getStringList("Item." + (info ? "info" : "lore")));
        lore.replaceAll((s) -> MessageUtil.formatText(pokemon, s));
        ItemMeta im = item.getItemMeta();
        boolean isShiny = pokemon.isShiny();
        im.setDisplayName((main.getConfig().getBoolean("Item.shiny.enable") && isShiny ? main.getConfig().getString("Item.shiny.header")
                .replace("&", "§") : "") + main.getConfig().getString("Item.name").replace("&", "§")
                .replace("%pokemon_name%", PokemonUtil.getPokemonName(pokemon.getSpecies())));
        im.setLore(lore);
        item.setItemMeta(im);
        net.minecraft.server.v1_12_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound nbtTagCompound = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
        assert nbtTagCompound != null;
        nbtTagCompound.set("pokemon", new NBTTagInt(slot));
        nmsItemStack.setTag(nbtTagCompound);
        item = CraftItemStack.asBukkitCopy(nmsItemStack);
        return item;
    }

    public void sendCaptureText(Player player, Pokemon pokemon, String es) {
        String[] split = getConfig().getString("capture.text").replace("%player%", player.getName())
                .replace("%type%", captureList.get(es)).replace("&", "§").split("%pokemon%");
        TextComponent header = new TextComponent(split[0]);
        TextComponent pt = new TextComponent(PokemonUtil.getPokemonName(pokemon.getSpecies()));
        List<String> texts = getConfig().getStringList("capture.hover");
        TextComponent[] showTexts = new TextComponent[texts.size()];
        for (int i = 0; i < texts.size(); i++) {
            showTexts[i] = new TextComponent(Blank038API.getPokemonAPI().getStatsHelper().format(pokemon, texts.get(i)) + ((i + 1) == texts.size() ? "" : "\n"));
        }
        pt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, showTexts));
        TextComponent end = new TextComponent();
        if (split.length > 1) {
            end = new TextComponent(split[1]);
        }
        header.addExtra(pt);
        header.addExtra(end);
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.spigot().sendMessage(header);
        }
    }

    public void reloadCaptureList() {
        captureList.clear();
        for (String l : getConfig().getStringList("capture.list")) {
            String[] fg = l.split("//");
            captureList.put(fg[0], fg[1].replace("&", "§"));
        }
        spawnTypeList.clear();
        for (String l : getConfig().getStringList("pokemonSpawn.list")) {
            String[] fg = l.split("//");
            spawnTypeList.put(fg[0], fg[1].replace("&", "§"));
        }
    }

    public boolean inCaptureList(String key) {
        return captureList.containsKey(key);
    }

    public boolean inSpawnList(String key) {
        return spawnTypeList.containsKey(key);
    }

    public void sendLegendSpawnText(EntityPixelmon ep) {
        if (getConfig().getBoolean("legendSpawn.enable")) {
            new PokemonSpawnModel(getConfig().getConfigurationSection("legendSpawn"), ep, "");
        }
    }

    public void sendPokemonSpawnText(EntityPixelmon ep) {
        if (getConfig().getBoolean("pokemonSpawn.enable")) {
            String type = spawnTypeList.getOrDefault(ep.getSpecies().name(), "");
            new PokemonSpawnModel(getConfig().getConfigurationSection("pokemonSpawn"), ep, type);
        }
    }
}
