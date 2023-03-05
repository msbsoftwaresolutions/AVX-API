package net.arvaux.core.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.arvaux.core.Main;
import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.entity.Player;

public class UtilBungee {

    public static void sendPlayer(Player player, String serverInString) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("Connect");
        dataOutput.writeUTF(serverInString);
        player.sendPluginMessage(Main.getInstance(), "BungeeCord", dataOutput.toByteArray());
    }

}
