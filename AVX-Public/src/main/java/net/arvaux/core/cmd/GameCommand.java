package net.arvaux.core.cmd;

import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand {
    private String[] _field_a; //name
    private Command _field_b;
    private String _field_c; //label
    private CommandSender _field_d;

    public GameCommand(CommandSender var_1, org.bukkit.command.Command var_2, String var_3, String[] var_4,
                       int subCommand) {
        String[] modArgs = new String[var_4.length - subCommand];
        for (int i = 0; i < var_4.length - subCommand; i++) {
            modArgs[i] = var_4[i + subCommand];
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(var_3);
        for (int x = 0; x < subCommand; x++) {
            buffer.append("." + var_4[x]);
        }
        String cmdLabel = buffer.toString();
        this._field_d = var_1;
        this._field_b = var_2;
        this._field_c = cmdLabel;
        this._field_a = modArgs;
    }

    public String[] getArgs() {
        return this._field_a;
    }

    public String getArgs(int index) {
        return this._field_a[index];
    }

    public Command getCommand() {
        return this._field_b;
    }

    public CommandHelpCenter getHelp() {
        CommandHelpCenter help = new CommandHelpCenter(this.getCommand().getName());
        return help;
    }

    public String getLabel() {
        return this._field_c;
    }

    public GamePlayer getPlayer() {
        return new GamePlayer(((Player) _field_d).getUniqueId());
    }

    public CommandSender getSender() {
        return this._field_d;
    }

    public boolean isPlayer() {
        return this._field_d instanceof Player;
    }

    public int size() {
        return this._field_a.length;
    }
}

