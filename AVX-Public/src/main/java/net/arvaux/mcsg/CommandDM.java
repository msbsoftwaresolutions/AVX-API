package net.arvaux.mcsg;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.ess.CommandForcestart;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.module.Module;
import net.arvaux.core.util.M;
import net.arvaux.core.ess.GameState;

public class CommandDM implements ICommand {

    @Command(name = "dm", aliases = "deathmatch", permission = Group.PARTNER)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        if (player.hasGroup(Group.MOD) || player.hasGroup(Group.SRMOD)) {
            player.send(M.COMMAND_NO_PERMS);
            return false;
        }
        if (!Module.isModule(Module.MCSG)) {
            player.send(M.COMMAND_MOD_DISABLED);
            return false;
        }
        if(GameState.isState(GameState.INGAME)) {
            if (SGBase.FORCE_DM == false) {
                if (!player.hasPermission(Group.ADMIN)) {
                    if (SGBase.TIME_MANAGER.inGame > 900) {
                        int t = SGBase.TIME_MANAGER.inGame - 900;
                        player.send(M.MCSG + "§cYou must wait 5 minutes before forcing deathmatch!");
                        return false;
                    }
                }
                SGBase.TIME_MANAGER.inGame = 10;
                SGBase.FORCE_DM = true;
                CommandForcestart.dmMessage(player);
            } else {
                player.send(M.GAME_EVENT_RUNNING);
            }
        } else {
            player.send(M.GAME_WRONG_STATE);
        }
        return false;
    }
}
