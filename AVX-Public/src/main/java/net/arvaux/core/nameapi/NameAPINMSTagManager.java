package net.arvaux.core.nameapi;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.arvaux.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class NameAPINMSTagManager extends NameAPIReflectUtils {

    public Object getAsCraftChatMessage(String s) {
        return getCraftClass("util.CraftChatMessage");
    }

    public void updatePlayerListName(Player p, String name) {
        try {
            Class<?> enumPlayerInfoAction = Bukkit.getServer().getVersion().equals("1_8_R1")
                    ? getNMSClass("EnumPlayerInfoAction")
                    : getNMSClass("PacketPlayOutPlayerInfo")
                    .getDeclaredClasses()[(Bukkit.getServer().getVersion().equals("1_11_R1")
                    || Bukkit.getServer().getVersion().equals("1_12_R1") || NameAPIBase.UTILS.isNewVersion()) ? 1
                    : 2];
            Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
            Object entityPlayerArray = Array.newInstance(entityPlayer.getClass(), 1);

            Array.set(entityPlayerArray, 0, entityPlayer);

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                try {
                    Field f = getNMSClass("EntityPlayer").getDeclaredField("listName");
                    f.setAccessible(true);
                    f.set(entityPlayer, getAsCraftChatMessage(name));

                    sendPacket(p,
                            getNMSClass("PacketPlayOutPlayerInfo")
                                    .getConstructor(enumPlayerInfoAction, entityPlayerArray.getClass())
                                    .newInstance(enumPlayerInfoAction.getDeclaredField("UPDATE_DISPLAY_NAME")
                                            .get(enumPlayerInfoAction), entityPlayerArray),
                            false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateName(Player p, String nickName) {
        try {
            Object gameProfile = p.getClass().getMethod("getProfile").invoke(p);
            Field nameField = NameAPIBase.UTILS.getNameField();
            nameField.setAccessible(true);
            nameField.set(gameProfile, nickName);
            nameField.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUniqueId(Player p, UUID uniqueId) {
        if (uniqueId == null)
            uniqueId = p.getUniqueId();

        try {
            Object gameProfile = p.getClass().getMethod("getProfile").invoke(p);
            Field uuidField = NameAPIBase.UTILS.getUUIDField();
            uuidField.setAccessible(true);
            uuidField.set(gameProfile, uniqueId);
            uuidField.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSkin(Player p, String skinName) {
        try {
            GameProfile gameProfile = (GameProfile) p.getClass().getMethod("getProfile").invoke(p);

            gameProfile.getProperties().removeAll("textures");

            GameProfile gp = null;

            try {
                gp = NameAPIBase.GAME_PROFILE_BUILDER.fetch(new NameAPIUUIDFetcher().getUUID(skinName));
            } catch (Exception e) {
                //p.sendMessage(
                //		M.EXCEPTION_PREFIX + "Mojang skin profile couldn't load, please contact an administrator.");
            }

            if (gp == null)
                gp = NameAPIBase.UTILS.getDefaultGameProfile();

            Collection<Property> props = gp.getProperties().get("textures");
            gameProfile.getProperties().putAll("textures", props);

        } catch (

                Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSkin_1_8_R1(Player p, String skinName) {
        try {
            GameProfile gameProfile = (GameProfile) p.getClass().getMethod("getProfile").invoke(p);

            gameProfile.getProperties().removeAll("textures");

            GameProfile gp = null;

            try {
                gp = NameAPIBase.GAME_PROFILE_BUILDER.fetch(new NameAPIUUIDFetcher().getUUID(skinName));
            } catch (Exception e) {
                //p.sendMessage(
                //		M.EXCEPTION_PREFIX + "Mojang skin profile couldn't load, please contact an administrator.");
            }

            if (gp == null)
                gp = NameAPIBase.UTILS.getDefaultGameProfile();

            Collection<Property> props = gp.getProperties().get("textures");
            gameProfile.getProperties().putAll("textures", props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void updatePlayer(Player p, UpdateType type, String skinName, boolean forceUpdate) {
        String version = NameAPIBase.VERSION;
        NameAPI api = new NameAPI(p);
        //String nickName = api.getNickName();

        try {
            Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
            Object entityPlayerArray = Array.newInstance(entityPlayer.getClass(), 1);
            Array.set(entityPlayerArray, 0, entityPlayer);

            Object packetEntityDestroy = getNMSClass("PacketPlayOutEntityDestroy").getConstructor(int[].class)
                    .newInstance(new int[]{p.getEntityId()});
            Object packetPlayOutPlayerInfoRemove;

            sendPacket(p, packetEntityDestroy, forceUpdate);

            Class<?> enumPlayerInfoAction = (version.equals("1_8_R1") ? getNMSClass("EnumPlayerInfoAction")
                    : getNMSClass("PacketPlayOutPlayerInfo")
                    .getDeclaredClasses()[(version.startsWith("1_1") && !(version.equals("1_10_R1"))) ? 1 : 2]);

            packetPlayOutPlayerInfoRemove = getNMSClass("PacketPlayOutPlayerInfo")
                    .getConstructor(enumPlayerInfoAction, entityPlayerArray.getClass())
                    .newInstance(enumPlayerInfoAction.getDeclaredField("REMOVE_PLAYER").get(enumPlayerInfoAction),
                            entityPlayerArray);

            sendPacket(p, packetPlayOutPlayerInfoRemove, forceUpdate);

            if (!(type.equals(UpdateType.QUIT))) {
                if (!(Main.getInstance().isEnabled()) || !(p.isOnline()))
                    return;

                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    try {
                        Object packetNamedEntitySpawn;
                        Object packetPlayOutPlayerInfoAdd;
                        Object worldClient = entityPlayer.getClass().getMethod("getWorld").invoke(entityPlayer);
                        Object worldData = worldClient.getClass().getMethod("getWorldData").invoke(worldClient);
                        Object interactManager = entityPlayer.getClass().getField("playerInteractManager")
                                .get(entityPlayer);
                        Object packetRespawnPlayer;

                        api.changeSkin(skinName);

                        if (version.startsWith("1_16")) {
                            Object craftWorld = p.getWorld().getClass().getMethod("getHandle").invoke(p.getWorld());
                            Class<?> enumGameMode = getNMSClass("EnumGamemode");

                            packetRespawnPlayer = version.equals("1_16_R1") ? getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(getNMSClass("ResourceKey"), getNMSClass("ResourceKey"), long.class,
                                            enumGameMode, enumGameMode, boolean.class, boolean.class, boolean.class)
                                    .newInstance(craftWorld.getClass().getMethod("getTypeKey").invoke(craftWorld),
                                            craftWorld.getClass().getMethod("getDimensionKey").invoke(craftWorld),
                                            getNMSClass("BiomeManager").getMethod("a", long.class).invoke(null,
                                                    p.getWorld().getSeed()),
                                            interactManager.getClass().getMethod("getGameMode").invoke(interactManager),
                                            interactManager.getClass().getMethod("c").invoke(interactManager),
                                            craftWorld.getClass().getMethod("isDebugWorld").invoke(craftWorld),
                                            craftWorld.getClass().getMethod("isFlatWorld").invoke(craftWorld), true)
                                    : getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(getNMSClass("DimensionManager"), getNMSClass("ResourceKey"),
                                            long.class, enumGameMode, enumGameMode, boolean.class,
                                            boolean.class, boolean.class)
                                    .newInstance(
                                            craftWorld.getClass().getMethod("getDimensionManager")
                                                    .invoke(craftWorld),
                                            craftWorld.getClass().getMethod("getDimensionKey")
                                                    .invoke(craftWorld),
                                            getNMSClass("BiomeManager").getMethod("a", long.class).invoke(null,
                                                    p.getWorld().getSeed()),
                                            interactManager.getClass().getMethod("getGameMode")
                                                    .invoke(interactManager),
                                            interactManager.getClass().getMethod("c").invoke(interactManager),
                                            craftWorld.getClass().getMethod("isDebugWorld").invoke(craftWorld),
                                            craftWorld.getClass().getMethod("isFlatWorld").invoke(craftWorld),
                                            true);
                        } else if (version.startsWith("1_15")) {
                            Class<?> dimensionManager = getNMSClass("DimensionManager");
                            Class<?> worldType = getNMSClass("WorldType");
                            Class<?> enumGameMode = getNMSClass("EnumGamemode");

                            packetRespawnPlayer = getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(dimensionManager, long.class, worldType, enumGameMode).newInstance(
                                            dimensionManager.getMethod("a", int.class).invoke(dimensionManager,
                                                    p.getWorld().getEnvironment().getId()),
                                            Hashing.sha256().hashLong(p.getWorld().getSeed()).asLong(),
                                            worldType.getMethod("getType", String.class).invoke(worldType,
                                                    p.getWorld().getWorldType().getName()),
                                            enumGameMode.getMethod("getById", int.class).invoke(enumGameMode,
                                                    p.getGameMode().getValue()));
                        } else if (version.startsWith("1_14")) {
                            Class<?> dimensionManager = getNMSClass("DimensionManager");
                            Class<?> worldType = getNMSClass("WorldType");
                            Class<?> enumGameMode = getNMSClass("EnumGamemode");

                            packetRespawnPlayer = getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(dimensionManager, worldType, enumGameMode).newInstance(
                                            dimensionManager.getMethod("a", int.class).invoke(dimensionManager,
                                                    p.getWorld().getEnvironment().getId()),
                                            worldType.getMethod("getType", String.class).invoke(worldType,
                                                    p.getWorld().getWorldType().getName()),
                                            enumGameMode.getMethod("getById", int.class).invoke(enumGameMode,
                                                    p.getGameMode().getValue()));
                        } else if (version.equals("1_13_R2")) {
                            Object craftWorld = p.getWorld().getClass().getMethod("getHandle").invoke(p.getWorld());

                            packetRespawnPlayer = getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(getNMSClass("DimensionManager"), getNMSClass("EnumDifficulty"),
                                            getNMSClass("WorldType"), getNMSClass("EnumGamemode"))
                                    .newInstance(worldClient.getClass().getDeclaredField("dimension").get(craftWorld),
                                            worldClient.getClass().getMethod("getDifficulty").invoke(worldClient),
                                            worldData.getClass().getMethod("getType").invoke(worldData), interactManager
                                                    .getClass().getMethod("getGameMode").invoke(interactManager));
                        } else {
                            Class<?> enumGameMode = (version.equals("1_8_R2") || version.equals("1_8_R3")
                                    || version.equals("1_9_R1") || version.equals("1_9_R2"))
                                    ? getNMSClass("WorldSettings").getDeclaredClasses()[0]
                                    : getNMSClass("EnumGamemode");

                            packetRespawnPlayer = getNMSClass("PacketPlayOutRespawn")
                                    .getConstructor(int.class, getNMSClass("EnumDifficulty"), getNMSClass("WorldType"),
                                            enumGameMode)
                                    .newInstance(p.getWorld().getEnvironment().getId(), (version.equals("1_7_R4")
                                                    ? getNMSClass("World").getDeclaredField("difficulty").get(worldClient)
                                                    : worldClient.getClass().getMethod("getDifficulty").invoke(worldClient)),
                                            worldData.getClass().getMethod("getType").invoke(worldData), interactManager
                                                    .getClass().getMethod("getGameMode").invoke(interactManager));
                        }

                        sendPacketNMS(p, packetRespawnPlayer);

                        packetNamedEntitySpawn = getNMSClass("PacketPlayOutNamedEntitySpawn")
                                .getConstructor(getNMSClass("EntityHuman")).newInstance(entityPlayer);

                        if (version.equals("1_7_R4")) {
                            Class<?> playOutPlayerInfo = getNMSClass("PacketPlayOutPlayerInfo");

                            packetPlayOutPlayerInfoAdd = playOutPlayerInfo
                                    .getMethod("addPlayer", getNMSClass("EntityPlayer"))
                                    .invoke(playOutPlayerInfo, entityPlayer);
                        } else {
                            Class<?> enumPlayerInfoAction1 = (version.equals("1_8_R1")
                                    ? getNMSClass("EnumPlayerInfoAction")
                                    : getNMSClass("PacketPlayOutPlayerInfo")
                                    .getDeclaredClasses()[(version.startsWith("1_1")
                                    && !(version.equals("1_10_R1"))) ? 1 : 2]);

                            packetPlayOutPlayerInfoAdd = getNMSClass("PacketPlayOutPlayerInfo")
                                    .getConstructor(enumPlayerInfoAction1, entityPlayerArray.getClass())
                                    .newInstance(enumPlayerInfoAction1.getDeclaredField("ADD_PLAYER")
                                            .get(enumPlayerInfoAction1), entityPlayerArray);
                        }

                        p.teleport(new Location(p.getWorld(), p.getLocation().getX(), p.getLocation().getY(),
                                p.getLocation().getZ(), p.getLocation().getYaw(), p.getLocation().getPitch()));
                        p.updateInventory();

                        if (!(Main.getInstance().isEnabled()) || !(p.isOnline()))
                            return;

                        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                            sendPacket(p, packetPlayOutPlayerInfoAdd, forceUpdate);
                            sendPacketExceptSelf(p, packetNamedEntitySpawn, forceUpdate);

                            try {
                                Object packetEntityLook = ((version.equals("1_7_R4") || version.equals("1_8_R1"))
                                        ? getNMSClass("PacketPlayOutEntityLook")
                                        : getNMSClass("PacketPlayOutEntity").getDeclaredClasses()[0])
                                        .getConstructor(int.class, byte.class, byte.class, boolean.class)
                                        .newInstance(p.getEntityId(),
                                                (byte) ((int) (p.getLocation().getYaw() * 256.0F / 360.0F)),
                                                (byte) ((int) (p.getLocation().getPitch() * 256.0F / 360.0F)),
                                                true);
                                Object packetHeadRotation = getNMSClass("PacketPlayOutEntityHeadRotation")
                                        .newInstance();
                                setField(packetHeadRotation, "a", p.getEntityId());
                                setField(packetHeadRotation, "b",
                                        (byte) ((int) (p.getLocation().getYaw() * 256.0F / 360.0F)));

                                sendPacketExceptSelf(p, packetEntityLook, forceUpdate);
                                sendPacketExceptSelf(p, packetHeadRotation, forceUpdate);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }, 4);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }, 1);
            }

            if (!(version.equals("1_7_R4") || version.equals("1_8_R1") || version.equals("1_8_R2")))
                updatePlayerCache(p);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void updatePlayerCache(Player p) {
        try {
            Class<?> minecraftServer = getNMSClass("MinecraftServer");
            Object server = minecraftServer.getMethod("getServer").invoke(minecraftServer);
            Object playerList = server.getClass().getMethod("getPlayerList").invoke(server);
            Field f = getNMSClass("PlayerList").getDeclaredField("playersByName");

            f.setAccessible(true);

            Map<String, Object> map = (Map<String, Object>) f.get(playerList);
            ArrayList<String> toRemove = new ArrayList<>();

            for (String cachedName : map.keySet()) {
                if (cachedName != null) {
                    Object entityPlayer = map.get(cachedName);

                    if ((entityPlayer == null) || entityPlayer.getClass().getMethod("getUniqueID").invoke(entityPlayer)
                            .equals(p.getUniqueId()))
                        toRemove.add(cachedName);
                }
            }

            for (String string : toRemove)
                map.remove(string);

            map.put(NameAPIBase.VERSION.startsWith("1_16") ? p.getName().toLowerCase(Locale.ROOT) : p.getName(),
                    p.getClass().getMethod("getHandle").invoke(p));

            f.set(playerList, map);
            f.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Player p, Object packet, boolean forceUpdate) {
        for (Player all : Bukkit.getOnlinePlayers()) {
            if ((all.canSee(p) && all.getWorld().getName().equals(p.getWorld().getName())) || forceUpdate) {
                sendPacketNMS(all, packet);
            }
        }
    }

    public void sendPacketExceptSelf(Player p, Object packet, boolean forceUpdate) {
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (all.getWorld().getName().equals(p.getWorld().getName()) || forceUpdate) {
                if (all.getEntityId() != p.getEntityId()) {
                    sendPacketNMS(all, packet);
                }
            }
        }
    }

    public void sendPacketNMS(Player p, Object packet) {
        try {
            Object handle = p.getClass().getMethod("getHandle").invoke(p);
            Object playerConnection = handle.getClass().getDeclaredField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum UpdateType {

        NICK, UNNICK, QUIT, UPDATE;

    }

}