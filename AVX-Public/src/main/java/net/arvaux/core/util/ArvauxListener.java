package net.arvaux.core.util;

import net.arvaux.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface ArvauxListener extends Listener {

    default void startListening() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    default void stopListening() {
        HandlerList.unregisterAll(this);
    }
}
