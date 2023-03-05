package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

public class CommandStaffMode implements ICommand {

    @Command(name = "staffmode", aliases = "sm", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        String[] args = command.getArgs();
        if(player.isStaffMode()) {
            player.setStaffMode(false);
            player.send(M.t(M.PUNISHMENT_PREFIX, "Staff Mode", true));
        } else if(!player.isStaffMode()) {
            player.setStaffMode(true);
            player.send(M.t(M.PUNISHMENT_PREFIX,"Staff Mode", false));
        }
        return false;
    }
}
