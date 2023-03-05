package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

import java.util.ArrayList;
import java.util.UUID;

public class CommandViewCommands implements ICommand {

    public static ArrayList<UUID> VIEW_COMMAND_LIST = new ArrayList<UUID>();

    @Command(name = "vc", aliases = "viewcommands", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        String[] args = command.getArgs();
        GamePlayer player = command.getPlayer();
        if (args.length > 0) {
            CommandHelpCenter help = new CommandHelpCenter(command.getCommand().getName());
            help.send(command.getSender());
            return false;
        } else {
            if (VIEW_COMMAND_LIST.contains(player.getUUID())) {
                VIEW_COMMAND_LIST.remove(player.getUUID());
                player.send(M.t(M.GXC, "View executed commands", false));
            } else {
                VIEW_COMMAND_LIST.add(player.getUUID());
                player.send(M.t(M.GXC, "View executed commands", true));
            }
        }
        return false;
    }
}
