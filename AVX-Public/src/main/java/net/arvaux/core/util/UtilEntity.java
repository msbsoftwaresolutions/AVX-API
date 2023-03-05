package net.arvaux.core.util;

import com.google.common.collect.Lists;
import net.arvaux.core.util.PathfinderGoalConstantMove;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UtilEntity implements Listener {

    private static final Field GOAL_FIELD_1 = UtilReflection.getField(PathfinderGoalSelector.class, true, "b");
    private static final Field GOAL_FIELD_2 = UtilReflection.getField(PathfinderGoalSelector.class, true, "c");

    public static Location getHeadLocation(Entity entity) {
        return getHeadLocation(UtilNMS.getNMSEntity((CraftEntity) entity));
    }

    public static Location getHeadLocation(net.minecraft.server.v1_8_R3.Entity entity) {
        Location location = new Location(entity.getWorld().getWorld(), entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
        return location.add(0, entity.getHeadHeight(), 0);
    }

    public static boolean isIn(AxisAlignedBB boundingBox, Location location) {
        return boundingBox.a(new Vec3D(location.getX(), location.getY(), location.getZ()));
    }

    public static boolean isBehind(Entity entity, Entity behind) {
        Vector between = UtilVector.between(entity.getLocation(), behind.getLocation());
        float playerYaw = entity.getLocation().getYaw();
        float betweenYaw = (float) UtilVector.getYaw(between);
        return Math.abs(playerYaw - betweenYaw) > 90;
    }

    public static boolean isInFrontOf(Entity entity, Entity inFront) {
        Vector between = UtilVector.between(entity.getLocation(), inFront.getLocation());
        float playerYaw = entity.getLocation().getYaw();
        float betweenYaw = (float) UtilVector.getYaw(between);
        return Math.abs(playerYaw - betweenYaw) < 90;
    }

    public static void clearGoals(PathfinderGoalSelector... goalSelectors) {
        assert GOAL_FIELD_1 != null;
        assert GOAL_FIELD_2 != null;

        if (goalSelectors == null) {
            return;
        }

        for (PathfinderGoalSelector selector : goalSelectors) {
            try {
                List<?> list1 = (List<?>) GOAL_FIELD_1.get(selector);
                List<?> list2 = (List<?>) GOAL_FIELD_2.get(selector);

                assert list1 != null;
                assert list2 != null;

                list1.clear();
                list2.clear();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static void setPathfinderGoal(EntityInsentient insentient, int index, PathfinderGoal goal) {
        insentient.goalSelector.a(index, goal);
    }

    public static void setTargetfinderGoal(EntityInsentient insentient, int index, PathfinderGoal goal) {
        insentient.targetSelector.a(index, goal);
    }

    public static List<Entity> getEntitiesInRadius(Location center, double radius) {
        double radiusSquare = UtilMath.squared(radius);
        World world = center.getWorld();

        return Lists.newArrayList(((CraftWorld) world).getHandle().entityList).stream()
                .map(net.minecraft.server.v1_8_R3.Entity::getBukkitEntity)
                .filter(Objects::nonNull)
                .filter(entity -> entity.getWorld().equals(center.getWorld()))
                .filter(entity -> entity.getLocation().distanceSquared(center) <= radiusSquare)
                .filter(entity -> !entity.isDead())
                .collect(Collectors.toList());
    }

    public static void respawnEntity(Player player, Entity entity) {
        PlayerConnection connection = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection;
        CraftEntity craftHandle = (CraftEntity) entity;

        EntityLiving living = (EntityLiving) craftHandle.getHandle();

        Packet packet = new PacketPlayOutEntityDestroy(living.getId());
        connection.sendPacket(packet);

        packet = new PacketPlayOutSpawnEntityLiving(living);
        connection.sendPacket(packet);
    }

    public static void performMovement(EntityInsentient entity, Vector movement) {
        clearGoals(entity.goalSelector, entity.targetSelector);
        entity.getNavigation().a(new BlockPosition(
                entity.locX + movement.getX(),
                entity.locY + movement.getZ(),
                entity.locZ + movement.getZ()
        ));
    }

    public static void constantMovement(EntityInsentient entity, Vector movement) {
        clearGoals(entity.goalSelector, entity.targetSelector);
        setPathfinderGoal(entity, 0, new PathfinderGoalConstantMove(entity, movement));
    }
}
