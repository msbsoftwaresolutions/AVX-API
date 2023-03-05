package net.arvaux.core.util;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UtilNMS {

    public static boolean isCustomEntity(org.bukkit.entity.Entity entity) {
        Entity nms = getNMSEntity((CraftEntity) entity);
        return !nms.getClass().getPackage().getName().contains("net.minecraft.server");
    }

    public static BlockPosition toBlockPosition(Location location) {
        return new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Location toLocation(World world, BlockPosition blockPosition) {
        return new Location(world, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public static Block toBlock(org.bukkit.block.Block block) {
        MaterialData data = new MaterialData(block);
        return Block.getByCombinedId(data.getCombinedID()).getBlock();
    }

    public static net.minecraft.server.v1_8_R3.World getNMSWorld(World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static WorldServer getNMSWorld(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        if (location.getWorld() == null) {
            throw new IllegalArgumentException("Location's world is null!");
        }

        return ((CraftWorld) location.getWorld()).getHandle();
    }

    public static EntityPlayer getNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static Entity getNMSEntity(CraftEntity entity) {
        return (Entity) entity.getHandle();
    }

}
