package net.arvaux.core.util.org.bukkit.event.server;

import net.arvaux.core.module.Module;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerSwitchModuleEvent extends Event {

    private static final HandlerList _handlers = new HandlerList();
    Module a;
    Module b;
    public ServerSwitchModuleEvent(Module module, Module old) {
        this.a = module;
        this.b = old;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public Module getModule() {
        return a;
    }

    public Module getLastModule() {
        return b;
    }

}