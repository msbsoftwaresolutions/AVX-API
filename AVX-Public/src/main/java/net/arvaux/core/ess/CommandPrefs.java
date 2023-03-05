package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.hub.menu.prefs.PreferencesSelector;

import java.sql.SQLException;

public class CommandPrefs implements ICommand {

    @Command(name = "prefs")
    public boolean k(GameCommand command) throws SQLException {
        new PreferencesSelector(null).open(command.getPlayer().bukkit());
        return false;
    }

}
