package net.arvaux.core.nameapi;

import com.mojang.authlib.GameProfile;
import net.arvaux.core.Main;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.module.PluginModule;
import net.arvaux.core.util.org.bukkit.event.player.PlayerTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NameAPIBase extends PluginModule {

    public static String VERSION = "XX_XX_RXX";
    public static NameAPIUtils UTILS;
    public static NameAPIReflectUtils REFLECT_UTILS;
    public static NameAPIGameProfileBuilder GAME_PROFILE_BUILDER;

    public static void toggleNickedAndDisguised(Player player) {
        if (!new GamePlayer(player.getUniqueId()).isDisguise() == true) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                NameAPI nicked = new NameAPI(all);
                if (nicked.isNicked() == true) {

                    // nicked.setName(nickName);

                    // nicked.unnickPlayerWithoutRemovingMySQL(false);

                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {

                        @Override
                        public void run() {
                            Bukkit.getPluginManager()
                                    .callEvent(new PlayerTagEvent(all, nicked.getNickName(), nicked.getNickName(),
                                            new GamePlayer(all.getUniqueId()).getDisplayGroup().getPrefix(),
                                            "", new GamePlayer(all.getUniqueId()).getDisplayGroup().getPrefix(),
                                            "", new GamePlayer(all.getUniqueId()).getDisplayGroup().getPrefix(),
                                            ""));
                        }
                    }, 5);
                }
            }
        }
    }

    @Override
    public void boot() {
        String reflectVersion = new NameAPINMSTagManager().getVersion();
        VERSION = reflectVersion.substring(1);
        UTILS = new NameAPIUtils();
        REFLECT_UTILS = new NameAPIReflectUtils();

        if (!(reflectVersion.equals("v1_7_R4") || reflectVersion.equals("v1_8_R1") || reflectVersion.equals("v1_8_R2")
                || reflectVersion.equals("v1_8_R3") || reflectVersion.equals("v1_9_R1")
                || reflectVersion.equals("v1_9_R2") || reflectVersion.equals("v1_10_R1")
                || reflectVersion.equals("v1_11_R1") || reflectVersion.equals("v1_12_R1")
                || reflectVersion.equals("v1_13_R1") || reflectVersion.equals("v1_13_R2")
                || reflectVersion.equals("v1_14_R1") || reflectVersion.equals("v1_15_R1")
                || reflectVersion.equals("v1_16_R1") || reflectVersion.equals("v1_16_R2"))) {
            // UTILS.sendConsole("§cERROR§8: §eVersion is §4§lINCOMPATIBLE§e!");

        } else {
            UTILS.setNameField(REFLECT_UTILS.getField(GameProfile.class, "name"));
            UTILS.setUUIDField(REFLECT_UTILS.getField(GameProfile.class, "id"));

            GAME_PROFILE_BUILDER = new NameAPIGameProfileBuilder();

        }

        for (Player all : Bukkit.getOnlinePlayers()) {
            if ((all != null) && (all.getUniqueId() != null)) {
                if (!(UTILS.getCanUseNick().containsKey(all.getUniqueId())))
                    UTILS.getCanUseNick().put(all.getUniqueId(), true);
            }
        }
    }

    @Override
    public Module m() {
        return Module.NONGAME;
    }

    @Override
    public void quit() {

    }

}
