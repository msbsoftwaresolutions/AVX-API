package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

public class CommandNight implements ICommand {

    @Command(name = "night", aliases = "lightsoff", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        String[] args = command.getArgs();
        player.send(M.u(M.GAME, "World Time", "NIGHT"));
        M.u(M.GAME, "World Time", "NIGHT");
        player.bukkit().getWorld().setTime(18000);
        return false;
    }
}
