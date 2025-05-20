package com.aiyostudio.pokemoninfo.internal.view;

import com.aiyostudio.pokemoninfo.internal.core.PokemonInfo;
import com.aiyostudio.pokemoninfo.internal.enums.ActionTypeEnum;
import com.aiyostudio.pokemoninfo.internal.i18n.I18n;
import com.aiyostudio.pokemoninfo.internal.manager.ActionCooldownManager;
import com.aiyostudio.pokemoninfo.internal.util.TextUtil;
import com.aystudio.core.bukkit.util.common.CommonUtil;
import com.aystudio.core.bukkit.util.inventory.GuiModel;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Blank038
 */
public class PartyView {
    @Setter
    @Getter
    private static FileConfiguration data;

    public static void init() {
        String sourceFile = MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1) ? "view/party.yml" : "view/legacy/party.yml";
        PokemonInfo.getInstance().saveResource(sourceFile, "view/party.yml", false, (file) -> {
            setData(YamlConfiguration.loadConfiguration(file));
        });
    }

    public static void open(Player player) {
        GuiModel model = new GuiModel(data.getString("title"), data.getInt("size"));
        model.registerListener(PokemonInfo.getInstance());
        model.setCloseRemove(true);
        Integer[] array = CommonUtil.formatSlots(data.getString("pokemon-slots"));
        for (int i = 0; i < array.length && i < 6; i++) {
            model.setItem(array[i], PartyView.getPokemonItem(player, i, data));
        }
        PartyView.initializeDisplayItem(model, data);
        model.execute((e) -> {
            e.setCancelled(true);
            if (e.getClickedInventory() != e.getInventory()) {
                return;
            }
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                return;
            }
            NBTItem nbtItem = new NBTItem(itemStack);
            if (!nbtItem.hasTag("PartyConvertPokemonIndex")) {
                return;
            }
            Player clicker = (Player) e.getWhoClicked();
            int pokemonSlot = nbtItem.getInteger("PartyConvertPokemonIndex");
            if (pokemonSlot < 0 || pokemonSlot >= 6) {
                return;
            }
            if (PokemonInfo.getModule().isNull(clicker.getUniqueId(), pokemonSlot, false)) {
                return;
            }
            if (data.contains("actions." + e.getClick().name())) {
                String action = data.getString("actions." + e.getClick().name(), "NONE");
                try {
                    ActionTypeEnum.valueOf(action.toUpperCase()).run(clicker, pokemonSlot);
                } catch (Exception ex) {
                    PokemonInfo.getInstance().getLogger().log(Level.WARNING, ex, () -> "行为出现异常: " + action);
                }
            }
        });
        model.openInventory(player);
    }

    private static void initializeDisplayItem(GuiModel model, FileConfiguration data) {
        if (data.getKeys(false).contains("items")) {
            for (String key : data.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection section = data.getConfigurationSection("items." + key);
                ItemStack itemStack = new ItemStack(Material.valueOf(section.getString("type")), section.getInt("amount"));
                ItemMeta meta = itemStack.getItemMeta();
                if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
                    ((Damageable) meta).setDamage((short) section.getInt("data"));
                    if (section.contains("custom-data")) {
                        meta.setCustomModelData(section.getInt("custom-data"));
                    }
                } else {
                    itemStack.setDurability((short) section.getInt("data"));
                }
                meta.setDisplayName(TextUtil.formatHexColor(section.getString("name")));
                List<String> lore = section.getStringList("lore");
                lore.replaceAll(TextUtil::formatHexColor);
                meta.setLore(lore);
                itemStack.setItemMeta(meta);

                for (int i : CommonUtil.formatSlots(section.getString("slot"))) {
                    model.setItem(i, itemStack);
                }
            }
        }
    }

    private static ItemStack getPokemonItem(Player player, int pokemonSlot, FileConfiguration data) {
        boolean isNullOrEgg = PokemonInfo.getModule().isNull(player.getUniqueId(), pokemonSlot, data.getBoolean("settings.egg-info"));
        Object pokemon = PokemonInfo.getModule().getPokemon(player.getUniqueId(), pokemonSlot);

        ConfigurationSection section = data.getConfigurationSection("pokemon-item." + (isNullOrEgg ? "empty" : "default"));
        ItemStack itemStack;
        if (isNullOrEgg) {
            itemStack = new ItemStack(Material.valueOf(section.getString("type")), section.getInt("amount"));
        } else {
            itemStack = PokemonInfo.getModule().getPokemonSpriteItem(pokemon);
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_13_R1)) {
            ((Damageable) meta).setDamage((short) section.getInt("data"));
            if (section.contains("custom-data")) {
                meta.setCustomModelData(section.getInt("custom-data"));
            }
        } else {
            itemStack.setDurability((short) section.getInt("data"));
        }
        meta.setDisplayName(TextUtil.formatHexColor(section.getString("name"))
                .replace("%pokemon_name%", PokemonInfo.getModule().getPokemonTranslationName(pokemon))
                .replace("%shiny%", I18n.getOption("shiny." + (PokemonInfo.getModule().isShiny(pokemon) ? "t" : "f"))));
        List<String> lore = PokemonInfo.getModule().internalFormat(pokemon, new ArrayList<>(section.getStringList("lore")));
        lore.replaceAll(TextUtil::formatHexColor);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger("PartyConvertPokemonIndex", pokemonSlot);
        return nbtItem.getItem();
    }

    public static void showPokemon(Player player, int pokemonSlot, ConfigurationSection options) {
        if (ActionCooldownManager.isCooldown("show", player.getName())) {
            player.sendMessage(I18n.getStrAndHeader("cooldown"));
            return;
        }
        if (PokemonInfo.getModule().isNull(player.getUniqueId(), pokemonSlot, false)) {
            player.sendMessage(I18n.getStrAndHeader("wrong-pokemon"));
            return;
        }
        ActionCooldownManager.setCooldown("show", player.getName(), options.getInt("cooldown") * 1000L);
        Object pokemon = PokemonInfo.getModule().getPokemon(player.getUniqueId(), pokemonSlot);
        String pokemonName = TextUtil.formatHexColor(options.getString("name"))
                .replace("%pokemon_name%", PokemonInfo.getModule().getPokemonTranslationName(pokemon));
        String message = options.getString("message").replace("%player%", player.getName());
        String[] split = TextUtil.formatHexColor(message).split("%pokemon%");
        List<String> stats = new ArrayList<>(), formatStats = PokemonInfo.getModule().internalFormat(pokemon, options.getStringList("description"));
        for (int i = 0; i < formatStats.size(); i++) {
            if (i + 1 == formatStats.size()) {
                stats.add(formatStats.get(i));
                break;
            }
            stats.add(formatStats.get(i) + "\n");
        }
        TextComponent textComponent = new TextComponent(split[0]),
                pokemonDesc = new TextComponent(pokemonName);
        pokemonDesc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, stats.stream()
                .map((s) -> new TextComponent(TextUtil.formatHexColor(s)))
                .toArray(TextComponent[]::new)));
        textComponent.addExtra(pokemonDesc);
        if (split.length > 1) {
            textComponent.addExtra(new TextComponent(split[1]));
        }
        Bukkit.getOnlinePlayers().forEach((p) -> p.spigot().sendMessage(textComponent));
    }
}
