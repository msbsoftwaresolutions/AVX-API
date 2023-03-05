package net.arvaux.build;

import net.arvaux.core.ess.CommandSpawnpoint;
import net.arvaux.core.ess.GameState;
import net.arvaux.core.module.Module;
import net.arvaux.core.module.PluginModule;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.server.ServerSQL;
import net.arvaux.core.util.WorldManager;
import net.arvaux.core.util.org.bukkit.event.server.ServerSwitchModuleEvent;
import net.arvaux.core.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class BuildBase extends PluginModule implements Listener {

    public static List<String> MSGS = new ArrayList<String>();
    public static boolean WARZONE;

    @Override
    public void boot() {
        this.getPluginManager().registerEvents(this, Main.getInstance());
        this.getPluginManager().registerEvents(new interactEvent(), Main.getInstance());
        this.switchModule(new ServerSwitchModuleEvent(Module.getModule(),
                Module.getModuleS(Main.getInstance().config.getString("last-module"))));
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        if (Module.isModule(Module.BUILD)) {
            BuildScoreboard.send(new GamePlayer(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler
    public void switchModule(ServerSwitchModuleEvent event) {
        if (event.getModule() == Module.BUILD) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(CommandSpawnpoint.getSpawnpoint());
            }
            GameState.setState(GameState.LOBBY);
            ServerSQL.add_info(Main.getInstance().getServerID().getName(), GameState.getState().name(), "GAMESTATE");
            for (Player s : Bukkit.getOnlinePlayers()) {
                GamePlayer server = new GamePlayer(s.getUniqueId());
                BuildScoreboard.send(server);
            }
        }
    }

    @Override
    public Module m() {
        return Module.BUILD;
    }

    @Override
    public void quit() {

    }

}
