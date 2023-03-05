package net.arvaux.core.util;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.menu.utils.Message;
import net.arvaux.core.menu.utils.MultiLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class Messaging {

    public static void send(CommandSender sender, Message message) {
        if (sender == null || message == null) {
            return;
        }

        if (sender instanceof Player) {
            Messaging.send((Player) sender, message);
            return;
        }

        sender.sendMessage(message.toString(MultiLanguageManager.DEFAULT));
    }

    public static void send(Player player, Message message) {
        if (player == null || message == null) {
            return;
        }

        sendString(player, message.toString(player));
    }

    public static void send(GamePlayer player, Message message) {
        send(Bukkit.getPlayer(player.getUUID()), message);
    }


    public static void send(Player player, Message... messages) {
        if (player == null || messages == null) {
            return;
        }

        for (final Message message : messages) {
            if (message == null) {
                continue;
            }

            send(player, message);
        }
    }

    public static void broadcast(Message... messages) {
        for (Message message : messages) {
            broadcast(message);
        }
    }

    public static void broadcast(Message message) {
        if (message == null) {
            return;
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            send(player, message);
        }
    }

    public static void broadcast(Message message, Predicate<Player> playerPredicate) {
        if (message == null) {
            return;
        }

        Bukkit.getServer().getOnlinePlayers().stream().filter(playerPredicate).forEach(player -> send(player, message));
    }

    public static void clear(Player player) {
        for (int i = 0; i < 100; i++) {
            player.sendMessage("\n");
        }
    }

    public static void sendString(Player player, String string) {
        string = colorEachWord(string);
        player.sendMessage(string);
    }

    public static String colorEachWord(String input) {
        if (input == null) {
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', input);
    }

}
