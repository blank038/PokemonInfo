package com.aiyostudio.pokemoninfo.message;

import com.aiyostudio.pokemoninfo.util.TextUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * @author Blank038
 */
public class CustomMessage {
    private TextComponent textComponent;

    public void setTextComponent(TextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public void broadcast() {
        if (textComponent != null) {
            Bukkit.getOnlinePlayers().forEach((p) -> p.spigot().sendMessage(textComponent));
        }
    }

    public static class Build {
        private List<String> hoverLines;
        private String message;
        private String pokemonName;

        public Build setMessage(String message) {
            this.message = TextUtil.formatHexColor(message);
            return this;
        }

        public Build setPokemonName(String pokemonName) {
            this.pokemonName = pokemonName;
            return this;
        }

        public Build setHolverLines(List<String> holverLines) {
            this.hoverLines = holverLines;
            return this;
        }

        public CustomMessage build() {
            if (message == null || hoverLines == null || pokemonName == null) {
                return null;
            }
            String[] split = this.message.split("%pokemon%");
            TextComponent textComponent = new TextComponent(split[0]),
                    pokemonDesc = new TextComponent(pokemonName);

            pokemonDesc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.hoverLines.stream()
                    .map((s) -> new TextComponent(TextUtil.formatHexColor(s)))
                    .toArray(TextComponent[]::new)));

            textComponent.addExtra(pokemonDesc);
            if (split.length > 1) {
                textComponent.addExtra(new TextComponent(split[1]));
            }

            CustomMessage customMessage = new CustomMessage();
            customMessage.setTextComponent(textComponent);
            return customMessage;
        }
    }
}
