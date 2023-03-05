package net.arvaux.core.cuboid;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.group.Group;

public class CommandCuboid implements ICommand {

    @Command(name = "cuboid", permission = Group.ADMIN)
    public boolean k(GameCommand command) {
        // todo give cuboid wand and open cuboid gui
        return false;
    }
}
