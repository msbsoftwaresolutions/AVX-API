package net.arvaux.mcsg;

import net.arvaux.core.cmd.*;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ess.GameState;
import net.arvaux.core.util.M;
import net.arvaux.mcsg.ui.UISpectator;
import net.arvaux.mcsg.ui.UISponsor;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandSponsor implements ICommand, ICompleter {

    @Command(name = "sponsor", aliases = "spons")
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        CommandHelpCenter help = command.getHelp();
        help.add("[player]", "Spectate player");

        if (command.getArgs().length == 1) {
            if (!GameState.isState(GameState.LOBBY)) {
                if (GameState.SPECTATING.contains(player.getUUID())) {
                    if (Bukkit.getPlayer(command.getArgs()[0]) != null) {
                        if (GameState.PLAYING.contains(Bukkit.getPlayer(command.getArgs()[0]).getUniqueId())) {
                            player.open(new UISponsor());
                            player.send(M.GAME+ "§3Showing sponsor menu...");
                        } else {
                            player.send(M.MCSG+ "§cYou cannot sponsor this player!");
                        }
                        return false;
                    } else {
                        player.send(M.MCSG+ "§cYou cannot sponsor this player!");
                    }
                } else {
                    player.send(M.MCSG + "§cYou can only do this while spectating!");
                }
            } else {
                player.send(M.GAME_WRONG_STATE);
            }
            return false;
        }

        help.send(command.getSender());
        return false;
    }

    @Command(name = "sponsor")
    public List<String> l(GameCommand command) {
        List<String> players = new ArrayList<>();
        if (command.getArgs().length > 0) {
            for (UUID uuid : GameState.PLAYING) {
                players.add(new GamePlayer(uuid).getDisplayName());
            }
            return players;
        }
        return null;
    }
}
