package net.arvaux.core.nameapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class NameAPIScoreboardTeamManager {

    private Player p;
    private String teamName, prefix, suffix;
    private Object packet;

    public NameAPIScoreboardTeamManager(Player p, String prefix, String suffix, int sortID, String rank) {
        this.p = p;
        this.prefix = prefix;
        this.suffix = suffix;
        this.teamName = sortID + rank + p.getUniqueId().toString().substring(0, 14);

        if (this.teamName.length() > 16)
            this.teamName = this.teamName.substring(0, 16);

        if (this.prefix == null)
            this.prefix = "";

        if (this.suffix == null)
            this.suffix = "";

        if (this.prefix.length() > 16)
            this.prefix = this.prefix.substring(0, 16);

        if (this.suffix.length() > 16)
            this.suffix = this.suffix.substring(0, 16);
    }

    public void destroyTeam() {
        try {
            packet = NameAPIBase.REFLECT_UTILS.getNMSClass("PacketPlayOutScoreboardTeam").getConstructor(new Class[0])
                    .newInstance(new Object[0]);

            try {
                NameAPIBase.REFLECT_UTILS.setField(packet, "a", teamName);
                NameAPIBase.REFLECT_UTILS.setField(packet, "f", 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            for (Player t : Bukkit.getOnlinePlayers())
                sendPacket(t, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTeam() {
        NameAPIReflectUtils reflectUtils = new NameAPIReflectUtils();

        try {
            for (Player t : Bukkit.getOnlinePlayers()) {
                packet = reflectUtils.getNMSClass("PacketPlayOutScoreboardTeam").getConstructor(new Class[0])
                        .newInstance(new Object[0]);

                String prefixForPlayer = prefix;
                String suffixForPlayer = suffix;
                List<String> contents = Arrays.asList(p.getName());

                reflectUtils.setField(packet, "a", teamName);
                reflectUtils.setField(packet, "b", teamName);
                reflectUtils.setField(packet, "c", prefixForPlayer);
                reflectUtils.setField(packet, "d", suffixForPlayer);
                reflectUtils.setField(packet, "e", contents);
                reflectUtils.setField(packet, "f", 0);
                reflectUtils.setField(packet, "g", 0);

                sendPacket(t, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * private Object getAsIChatBaseComponent(String txt) { NameAPIReflectUtils
     * reflectUtils = new NameAPIReflectUtils();
     *
     * try { return
     * reflectUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].
     * getMethod("a", String.class)
     * .invoke(reflectUtils.getNMSClass("IChatBaseComponent"), "{\"text\":\"" + txt
     * + "\"}"); } catch (IllegalAccessException | IllegalArgumentException |
     * InvocationTargetException | NoSuchMethodException | SecurityException e) {
     * e.printStackTrace(); }
     *
     * return null; }
     */

    private void sendPacket(Player p, Object packet) {
        try {
            Object playerHandle = p.getClass().getMethod("getHandle", new Class[0]).invoke(p, new Object[0]);
            Object playerConnection = playerHandle.getClass().getField("playerConnection").get(playerHandle);
            playerConnection.getClass()
                    .getMethod("sendPacket", new Class[]{new NameAPIReflectUtils().getNMSClass("Packet")})
                    .invoke(playerConnection, new Object[]{packet});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}