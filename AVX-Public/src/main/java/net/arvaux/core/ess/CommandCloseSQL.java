package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

public class CommandCloseSQL implements ICommand {

    @Command(name = "closesql", usage = "/closesql", inGameOnly = true, permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();

        CommandHelpCenter help = new CommandHelpCenter(command.getCommand().getName());

        if (command.getArgs().length == 0) {
            //GameSQL.closeConnection();
            player.send(M.GXC + "SQL Debug");
        } else {
            help.send(player, false);
        }

        return false;
    }

}
