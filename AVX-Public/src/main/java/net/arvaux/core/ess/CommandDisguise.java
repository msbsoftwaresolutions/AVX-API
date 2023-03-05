package net.arvaux.core.ess;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;

import java.util.HashMap;
import java.util.Map;

public class CommandDisguise implements ICommand {
    private Map<String, Long> disguiseUsage = new HashMap<String, Long>();

    @Command(name = "disguise", aliases = "d", usage = "/disguise", permission = Group.VIP)
    public boolean k(GameCommand command) {
        GamePlayer player = command.getPlayer();
        if (command.getArgs().length == 0) {

            if (!GameState.isState(GameState.LOBBY)) {
                player.send(M.COMMAND_MOD_DISABLED);
                return false;
            }

            long LastUsed = 0L;
            if (this.disguiseUsage.containsKey(player.getUUID().toString())) {
                LastUsed = ((Long) this.disguiseUsage.get(player.getUUID().toString())).longValue();
            }

            int cdmillis = 10000;
            if (System.currentTimeMillis() - LastUsed < cdmillis) {
                int timeLeft = (int) (10L - (System.currentTimeMillis() - LastUsed) / 1000L);
                player.send(M.COOLDOWN + "§c(" + timeLeft + "s)");
                return false;
            }
            this.disguiseUsage.put(player.getUUID().toString(), Long.valueOf(System.currentTimeMillis()));

            player.setRandomDisplayName();

            if (!player.hasPermission(Group.MOD) || player.hasGroup(Group.PARTNER))
                player.send(M.GXC_LOGGED);
            player.send(
                    M.t(M.GAME, "Disguise", true));
            player.send(M.GAME + "§3You are now disguised as " + Group.REGULAR.getColor() + "§l" + player.getDisplayName() + "!");

            return false;
        }
        CommandHelpCenter help = command.getHelp();
        help.add("Disguises your name");
        help.send(command.getSender());
        return false;
    }

    @Command(name = "undisguise", aliases = "ud", usage = "/undisguise", permission = Group.VIP)
    public boolean l(GameCommand command) {
        GamePlayer player = command.getPlayer();
        if (command.getArgs().length == 0) {

            if (!GameState.isState(GameState.LOBBY)) {
                player.send(M.COMMAND_MOD_DISABLED);
                return false;
            }

            player.setDisplayGroup(player.getGroup());
            player.setDisplayName(player.getName());
            player.setDisguised(false);

            if (!player.hasPermission(Group.MOD) || player.hasGroup(Group.PARTNER))
                player.send(M.GXC_LOGGED);
            player.send(M.t(M.GAME, "Disguise", false));
            return false;
        }
        CommandHelpCenter help = command.getHelp();
        help.add("Undisguises your name");
        help.send(command.getSender());
        return false;
    }

}
