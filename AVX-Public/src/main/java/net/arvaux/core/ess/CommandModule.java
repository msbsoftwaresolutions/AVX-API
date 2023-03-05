package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.ess.ui.mod.UIStoryModule;
import net.arvaux.core.group.Group;

public class CommandModule implements ICommand {

    @Command(name = "module", aliases = "mod", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        command.getPlayer().open(new UIStoryModule());
        return false;
    }

}
