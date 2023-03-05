package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

public class CommandSilentJoin implements ICommand {

    @Command(name = "silentjoin", permission = Group.PRO)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();

        CommandHelpCenter help = new CommandHelpCenter(command.getCommand().getName());

        if (command.getArgs().length > 0) {
            help.send(player, false);
        } else {
            // command.getPlayer().send(String.valueOf(player.isSilentJoin()));
            if (player.isSilentJoin() == true) {
                player.setSilentJoin(false);
                player.send(M.t(M.GAME, "Silent Join", false));
            } else {
                player.setSilentJoin(true);
                player.send(M.t(M.GAME, "Silent Join", true));
            }
        }

        return false;
    }

}