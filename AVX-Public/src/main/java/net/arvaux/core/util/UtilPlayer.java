package net.arvaux.core.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.arvaux.core.Main;
import net.arvaux.core.entity.player.Callback;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.menu.item.util.Common;
import net.arvaux.core.menu.item.util.UtilText;
import net.arvaux.core.menu.utils.Colors;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UtilPlayer {

    private static final LoadingCache<UUID, GameProfile> PROFILE_LOADING_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<UUID, GameProfile>() {
                @Override
                public GameProfile load(UUID uuid) {
                    return MinecraftServer.getServer().aD().fillProfileProperties(new GameProfile(uuid, null), true);
                }
            });

    public static final String TEXTURE_PROPERTY = "{\"timestamp\":%S,\"profileId\":\"%s\",\"profileName\":\"%s\",\"signatureRequired\":true,,\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
    private static final AtomicInteger RANDOM_PROFILE = new AtomicInteger(1);
    private static final String SKIN_PREFIX = "http://textures.minecraft.net/texture/";

    public static void wait(long ticks, boolean doActionBar, Player player, Runnable fail, Runnable success) {
        Location location = player.getLocation();

        new BukkitRunnable() {

            private long remaining = ticks;

            @Override
            public void run() {
                if (--this.remaining >= 0) {
                    if (UtilLocation.isSamePosition(location, player.getLocation())) {
                        if (!doActionBar) {
                            return;
                        }

                        PlayerActionBar.sendActionBarRaw(player, UtilText.getProgressBar(25, remaining, ticks, false) + " " + Colors.WHITE + UtilTime.tickToTimer((int) remaining));
                        return;
                    }

                    if (doActionBar) {
                        PlayerActionBar.sendActionBarRaw(player, Colors.RED + "Cancelled!");
                    }

                    fail.run();
                    cancel();
                    return;
                }

                success.run();
                cancel();
            }
        }.runTaskTimer(Main.getInstance(), 1L, 1L);
    }

    public static Player getPlayer(UUID uuid) {
        GamePlayer info = new GamePlayer(uuid);
        return info == null ? null : info.bukkit().getPlayer();
    }

    public static void setRandomUUID(GameProfile profile) {
        try {
            UtilReflection.setValue(profile, true, "id", UUID.randomUUID());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void setRandomName(GameProfile profile) {
        String name = RandomStringUtils.randomAlphabetic(8 + UtilMath.r(9));

        try {
            UtilReflection.setValue(profile, true, "name", name);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static GameProfile getNewProfile(String name) {
        return new GameProfile(UUID.randomUUID(), name);
    }

    public static Property getTextures(GameProfile profile) {
        PropertyMap map = profile.getProperties();
        return Iterables.getFirst(map.get("textures"), null);
    }

    public static String getTextureString(GameProfile profile) {
        Property texture = getTextures(profile);
        if (texture == null) {
            return null;
        }

        String textureString = new String(Base64.decodeBase64(texture.getValue().getBytes()));
        JsonObject json = Common.PARSER.parse(textureString).getAsJsonObject();
        if (!json.has("textures")) {
            return null; // If player is Steve
        }

        JsonObject textures = json.get("textures").getAsJsonObject();
        JsonObject skin = textures.get("SKIN").getAsJsonObject();
        String url = skin.get("url").getAsString();
        return url.substring(SKIN_PREFIX.length());
    }

    public static GameProfile setSkin(GameProfile profile, Collection<Property> properties) {
        PropertyMap map = profile.getProperties();
        map.removeAll("textures");
        map.putAll("textures", properties);
        return profile;
    }

    public static GameProfile setSkin(GameProfile profile, Property property) {
        PropertyMap map = profile.getProperties();
        map.removeAll("textures");
        map.put("textures", property);
        return profile;
    }

    public static GameProfile setSkin(GameProfile profile, String url) {
        url = SKIN_PREFIX + url;

        PropertyMap map = profile.getProperties();
        map.removeAll("textures");

        String formatted = String.format(TEXTURE_PROPERTY, System.currentTimeMillis(), profile.getId().toString().replace("-", ""), profile.getName(), url);
        byte[] encodedData = Base64.encodeBase64(formatted.getBytes());

        map.put("textures", new Property("textures", new String(encodedData)));

        return profile;
    }

    public static GameProfile getProfile(Player player) {
        Validate.notNull(player);
        return UtilNMS.getNMSPlayer(player).getProfile();
    }

    public static void getProfile(UUID uuid, Callback<GameProfile> callback) {
        UtilConcurrency.runAsync(() -> {
            GameProfile profile;
            if (Bukkit.getPlayer(uuid) != null) {
                profile = UtilNMS.getNMSPlayer(Bukkit.getPlayer(uuid)).getProfile();
            } else {
                try {
                    profile = PROFILE_LOADING_CACHE.getUnchecked(uuid);
                } catch (Exception e) {
                    SchedulerUtils.runLater(60 * 20L, () -> getProfile(uuid, callback));
                    return;
                }
            }

            UtilConcurrency.runSync(() -> callback.call(profile));
        });
    }

    public static GameProfile fromString(String string) {
        JsonObject object = Common.PARSER.parse(string).getAsJsonObject();

        UUID uuid = UUID.fromString(insertDashUUID(object.get("id").getAsString()));
        String name = object.get("name").getAsString();

        GameProfile profile = new GameProfile(uuid, name);
        PropertyMap map = Main.GSON.fromJson(object.get("properties"), PropertyMap.class);

        try {
            UtilReflection.setValue(profile, true, "properties", map);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return profile;
    }

    public static String insertDashUUID(String uuid) {
        if (uuid.contains("-")) {
            return uuid;
        }

        StringBuffer sb = new StringBuffer(uuid)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-");

        return sb.toString();
    }

    public static DamageCause getLastDamageCause(Player player) {
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null) {
            return null;
        }

        return lastDamage.getCause();
    }

    public static Player getKiller(Entity entity) {
        if (entity instanceof Player) {
            return getKiller((Player) entity);
        }

        EntityDamageEvent event = entity.getLastDamageCause();
        if (!(event instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        return getDamager((EntityDamageByEntityEvent) event);
    }

    public static Player getKiller(Player player) {
        return player.getKiller() != null ? player.getKiller() : getLastDamager(player);
    }

    public static Player getLastDamager(Player player) {
        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (!(lastDamage instanceof EntityDamageByEntityEvent)) {
            return null;
        }

        EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) lastDamage;
        return getDamager(damageEvent);
    }

    public static Player getDamager(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            return (Player) damager;
        }

        if (!(damager instanceof Projectile)) {
            return null;
        }

        Projectile projectile = (Projectile) damager;
        ProjectileSource source = projectile.getShooter();
        return !(source instanceof Player) ? null : (Player) source;
    }

    public static Player getClosestPlayer(Location location, double maxRange) {
        Objects.requireNonNull(location);

        List<Entity> entities = UtilEntity.getEntitiesInRadius(location, maxRange);
        List<Player> players = entities.stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .sorted(Comparator.comparingDouble(other -> location.distanceSquared(other.getLocation())))
                .collect(Collectors.toList());

        if (players.size() == 0) {
            return null;
        }

        return players.get(0);
    }

    public static Player getClosestPlayer(Player player) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            return null;
        }

        return Bukkit.getOnlinePlayers().stream()
                .filter(player::canSee)
                .filter(other -> other != player)
                .filter(other -> other.getWorld().equals(player.getWorld()))
                .min(Comparator.comparingDouble(other -> player.getLocation().distanceSquared(other.getLocation())))
                .orElse(null);
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        try {
            getConnection(player).sendPacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PlayerConnection getConnection(Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection;
    }
}
