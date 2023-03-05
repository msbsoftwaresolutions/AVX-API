package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;
import org.bukkit.Bukkit;

public class CommandFeed implements ICommand {

    @Command(name = "feed", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length == 1) {
            if (Bukkit.getPlayer(args[0]) == null) {
                player.send(M.EXCEPTION_PLAYER_OFFLINE);
                return false;
            }
            if (!GameState.isState(GameState.LOBBY) && !player.hasGroup(Group.OWNER)) {
                player.send(M.COMMAND_MOD_DISABLED);
                return false;
            }
            new GamePlayer(
                    Bukkit.getPlayer(args[0]).getUniqueId()).send(M.GAME + "Your hunger has been restored.");
            new GamePlayer(
                    Bukkit.getPlayer(args[0]).getUniqueId()).bukkit().setFoodLevel(20);
            player.send(M.GAME + new GamePlayer(
                    Bukkit.getPlayer(args[0]).getUniqueId()).getGroup().getColor() + "§l" + new GamePlayer(
                    Bukkit.getPlayer(args[0]).getUniqueId()).getName() + "§3's hunger has been restored.");

            return false;
        }
        player.send(M.GAME + "Your hunger has been restored.");
        player.bukkit().setFoodLevel(20);
        return false;
    }
}
