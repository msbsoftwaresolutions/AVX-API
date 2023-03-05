package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandClearChat implements ICommand {

    @Command(name = "clearchat", aliases = "cc", permission = Group.MOD)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        for (Player server : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i <= 1000; i++)
                server.sendMessage(" ");
            server.sendMessage(M.GAME + "The chat has been cleared.");
        }
        return false;
    }

}
