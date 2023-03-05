package net.arvaux.core.util;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UtilBlock {

    public static final BlockFace[] AXIS = {
            BlockFace.NORTH,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.EAST
    };

    public static final BlockFace[] RADIAL = {
            BlockFace.NORTH,
            BlockFace.NORTH_WEST,
            BlockFace.WEST,
            BlockFace.SOUTH_WEST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_EAST,
            BlockFace.EAST,
            BlockFace.NORTH_EAST
    };

    public static boolean isLiquid(Block block) {
        Material type = block.getType();
        return type == Material.LAVA
                || type == Material.WATER
                || type == Material.STATIONARY_LAVA
                || type == Material.STATIONARY_WATER;
    }

    public static boolean isTransparent(Block block) {
        return block == null
                || block.getType() == Material.AIR
                || block.getType() == Material.GLASS
                || block.getType() == Material.THIN_GLASS
                || block.getType() == Material.STAINED_GLASS
                || block.getType() == Material.STAINED_GLASS_PANE
                || !block.getType().isSolid();
    }

    public static void doBlockAction(Block block, int value) {
        Location location = block.getLocation();
        PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(UtilNMS.toBlockPosition(location), UtilNMS.toBlock(block), 1, value);
        for (Player player : location.getWorld().getPlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, false);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw                      angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return RADIAL[Math.floorMod(Math.round(-yaw / 45f), RADIAL.length)];
        }

        return AXIS[Math.floorMod(Math.round(-yaw / 90f), AXIS.length)];
    }

    public static List<Block> getBlocksInRadius(Location location, double radius, boolean solid) {
        List<Block> blocks = new ArrayList<>();

        int baseX = location.getBlockX();
        int baseY = location.getBlockY();
        int baseZ = location.getBlockZ();
        double radiusSquare = Math.pow(radius, 2);

        for (double x = baseX - radius; x <= baseX + radius; x++) {
            for (double y = baseY - radius; y <= baseY + radius; y++) {
                for (double z = baseZ - radius; z <= baseZ + radius; z++) {
                    double distSquare = (Math.pow(baseX - x, 2))
                            + (Math.pow(baseY - y, 2))
                            + (Math.pow(baseZ - z, 2));

                    if (distSquare > radiusSquare) {
                        continue;
                    }

                    Location current = new Location(location.getWorld(), x, y, z);
                    if (!current.getBlock().getType().isSolid() && solid) {
                        continue;
                    }

                    blocks.add(current.getBlock());
                }
            }
        }

        return blocks;
    }

    public static List<Block> getHighestBlocksInRadius(Location location, double radius, boolean solid) {
        return UtilBlock.getBlocksInRadius(location, radius, solid).stream()
                .filter(block -> block.equals(block.getLocation().getWorld().getHighestBlockAt(block.getLocation())))
                .collect(Collectors.toList());
    }

    public static Set<Block> getCircle(Location location, double radius) {
        Set<Block> blocks = new HashSet<>();
        for (int angle = 0; angle < 360; angle++) {
            blocks.add(
                    location.getWorld().getBlockAt(
                            location.clone().add(UtilVector.getPropulsiveVector(angle).multiply(radius))
                    )
            );
        }

        return blocks;
    }

    public static IBlockData getBlockData(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        return world.getType(position);
    }

    public static IBlockData getBlockData(Material material, byte data) {
        return CraftMagicNumbers.getBlock(material).fromLegacyData(data);
    }

    public static void sendBlockChangePacket(Player player, Location location, IBlockData data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
        PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(world, position);
        change.block = data;

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(change);
    }

    public static void sendBlockChangePacket(Location location, IBlockData data) {
        for (Player player : location.getWorld().getPlayers()) {
            sendBlockChangePacket(player, location, data);
        }
    }

    public static boolean hasLineOfSight(Location loc1, Location loc2) {
        Vector vector = loc2.clone().subtract(loc1).toVector().normalize();
        for (int i = 0; i < Math.ceil(loc1.distance(loc2)); i++) {
            loc1.add(vector);
            if (loc1.getBlock() == null || loc1.getBlock().getType() == Material.AIR) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static Vector toVector(BlockFace face) {
        return new Vector(face.getModX(), face.getModY(), face.getModZ());
    }

    public static boolean isType(Block block, Material... materials) {
        List<Material> materialList = Lists.newArrayList(materials);
        return materialList.contains(block.getType());
    }
}