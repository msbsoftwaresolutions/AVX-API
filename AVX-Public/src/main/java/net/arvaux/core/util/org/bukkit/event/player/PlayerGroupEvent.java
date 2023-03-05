package net.arvaux.core.util.org.bukkit.event.player;

import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerGroupEvent extends Event {

    private static final HandlerList _handlers = new HandlerList();
    GamePlayer a;

    public PlayerGroupEvent(GamePlayer player) {
        a = player;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public GamePlayer getPlayer() {
        return a;
    }

}