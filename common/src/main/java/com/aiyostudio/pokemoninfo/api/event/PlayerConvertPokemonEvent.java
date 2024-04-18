package com.aiyostudio.pokemoninfo.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * @author Blank038
 */
@Setter
@Getter
public class PlayerConvertPokemonEvent extends PlayerEvent
implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Object pokemon;
    private boolean cancelled, notify;

    public PlayerConvertPokemonEvent(Player who, Object pokemon) {
        super(who);
        this.pokemon = pokemon;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
