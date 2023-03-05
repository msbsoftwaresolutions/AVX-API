package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ess.ui.game.UIStoryGame;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

public class CommandGame implements ICommand {

    @Command(name = "game", aliases = "g", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        CommandHelpCenter help = command.getHelp();
        help.add("Opens the Game UI");

        if (!GameState.isState(GameState.LOBBY)) {
            player.send(M.GAME_WRONG_STATE);
            return false;
        }

        if (command.getArgs().length == 0) {
            player.open(new UIStoryGame());
            return false;
        }

        help.send(command.getPlayer(), false);

        return false;
    }
}
