package net.arvaux.core.cmd;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.util.M;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandHelpCenter {
    public String command;
    public List<String> line;

    public CommandHelpCenter(String command) {
        this.command = command;
        this.line = new ArrayList<String>();
    }

    public void add(String description) {
        this.line.add("§3" + description);
    }

    public void add(String arg, String description) {
        this.line.add(arg + " §3" + description);
    }

    public void send(CommandSender sender) {
        if (this.line.isEmpty()) {
            sender.sendMessage("§7# §8/§c" + this.command + " §3Executes /" + this.command);
        } else {
            for (String a : this.line)
                sender.sendMessage("§7# §8/§c" + this.command + " " + a);
        }
    }

    public void send(GamePlayer player, boolean single) {
        if (single == true) {
            player.send(M.HEADER);
            player.send(this.command);
            if (this.line.isEmpty()) {
                player.send("§7# §8/§c" + this.command + " §3Executes /" + this.command);
            } else {
                for (String a : this.line) {
                    player.send("§7# §8/§c" + a);
                }
            }
            player.send(M.HEADER);
        } else {
            if (this.line.isEmpty()) {
                player.send("§7# §8/§c" + this.command + " §3Executes /" + this.command);
            } else {
                for (String a : this.line) {
                    player.send("§7# §8/§c" + this.command + " " + a);
                }
            }
        }
    }
}
