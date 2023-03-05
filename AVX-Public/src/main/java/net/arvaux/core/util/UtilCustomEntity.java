package net.arvaux.core.util;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityTypes;
import net.minecraft.server.v1_8_R3.ReportedException;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Map;

public class UtilCustomEntity {

    public static void addToMaps(Class<? extends Entity> entityClass, EntityType type) {
        addToMaps(entityClass, entityClass.getSimpleName(), type.getTypeId());
    }

    public static void addToMaps(Class<? extends Entity> entityClass, String name, int id) {
        try {
            ((Map) UtilReflection.getValue(null, EntityTypes.class, true, "c")).put(name, entityClass);
            ((Map) UtilReflection.getValue(null, EntityTypes.class, true, "d")).put(entityClass, name);
            ((Map) UtilReflection.getValue(null, EntityTypes.class, true, "f")).put(entityClass, id);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void spawnEntity(Entity entity, Location loc) {
        if (loc.getChunk().isLoaded()) {
            entity.attachedToPlayer = true;
        }

        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entity.yaw = entity.lastYaw = loc.getYaw();
        entity.pitch = entity.lastPitch = loc.getPitch();

        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).aK = loc.getYaw();
            ((EntityLiving) entity).aI = loc.getYaw();
        }

        entity.dead = false;

        World world = ((CraftWorld) loc.getWorld()).getHandle();

        try {
            world.addEntity(entity, SpawnReason.CUSTOM);
        } catch (ReportedException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                return;
            }

            cause.printStackTrace();
        }
    }
}
