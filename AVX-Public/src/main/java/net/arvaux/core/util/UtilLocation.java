package net.arvaux.core.util;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class UtilLocation {

    public static boolean isInSameChunk(Location loc1, Location loc2) {
        int chunkX1 = loc1.getBlockX() >> 4;
        int chunkZ1 = loc1.getBlockZ() >> 4;
        int chunkX2 = loc2.getBlockX() >> 4;
        int chunkZ2 = loc2.getBlockZ() >> 4;
        return chunkX1 == chunkX2 && chunkZ1 == chunkZ2;
    }

    public static boolean isInChunk(Chunk chunk, Location location) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int x = location.getBlockX() >> 4;
        int z = location.getBlockZ() >> 4;
        return x == chunkX && chunkZ == z;
    }

    public static Location asBlockLocation(Location location) {
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static boolean safeDistanceCheck(Location loc1, Location loc2, double maxDist) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }

        double squared = UtilMath.squared(maxDist);
        return loc1.distanceSquared(loc2) <= squared;
    }

    public static boolean safeDistanceSquareCheck(Location loc1, Location loc2, double maxDist) {
        double distX = Math.abs(loc1.getX() - loc2.getX());
        double distZ = Math.abs(loc1.getZ() - loc2.getZ());
        return distX <= maxDist && distZ <= maxDist;
    }

    public static Location average(Location... locations) {
        return average(Lists.newArrayList(locations));
    }

    public static Location average(List<Location> locations) {
        if (locations.size() == 0) {
            throw new IllegalArgumentException("Empty list!");
        }

        World world = locations.get(0).getWorld();

        double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
        for (Location location : locations) {
            x += location.getX();
            y += location.getY();
            z += location.getZ();
            yaw += location.getYaw();
            pitch += location.getPitch();
        }

        x /= (double) locations.size();
        y /= (double) locations.size();
        z /= (double) locations.size();
        yaw /= (double) locations.size();
        pitch /= (double) locations.size();

        return new Location(world, x, y, z, (float) yaw, (float) pitch);
    }

    public static Location middle(Location loc1, Location loc2) {
        return loc1.clone().add(UtilVector.between(loc1, loc2).multiply(0.5));
    }

    public static Location toSurface(Location location) {
        Objects.requireNonNull(location);

        // Go to ground
        location.setY(location.getBlockY());

        Location highest = getHighestLocation(location);
        if (highest == null) {
            location = location.clone();
            location.setY(0);
            return location;
        }

        if (highest.getY() <= location.getY()) {
            return highest;
        }

        if (!isSolid(location)) {
            while (!isSolid(location) && location.getY() > 0) {
                location.subtract(0, 1, 0);
            }

            return location;
        }

        while (isSolid(location.clone().add(0, 1, 0))) {
            location.add(0, 1, 0);
        }

        return location;
    }

    public static boolean isSolid(Location location) {
        Objects.requireNonNull(location);

        return location.getBlock().getType().isSolid();
    }

    public static int getChunkX(Location location) {
        Objects.requireNonNull(location);

        return location.getBlockX() >> 4;
    }

    public static int getChunkZ(Location location) {
        Objects.requireNonNull(location);

        return location.getBlockZ() >> 4;
    }

    public static Location lookAt(Location from, Location target, boolean doPitch) {
        return lookAt(from, target, doPitch, false);
    }

    public static Location lookAt(Location from, Location target, boolean doPitch, boolean modify) {
        Vector vector = UtilVector.between(from, target);

        if (!modify) {
            from = from.clone();
        }

        double yaw = UtilVector.getYaw(vector);
        from.setYaw((float) yaw);

        if (doPitch) {
            double pitch = UtilVector.getPitch(vector);
            from.setPitch((float) pitch);
        }

        return from;
    }

    public static Location lookAwayFrom(Location from, Location target, boolean doPitch, boolean modify) {
        Vector vector = UtilVector.between(target, from);

        if (!modify) {
            from = from.clone();
        }

        double yaw = UtilVector.getYaw(vector);
        from.setYaw((float) yaw);

        if (doPitch) {
            double pitch = UtilVector.getPitch(vector);
            from.setPitch((float) pitch);
        }

        return from;
    }

    public static boolean isSameXYZ(Location loc1, Location loc2) {
        return loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
    }

    /**
     * Calculates the y delta between the two given locations
     *
     * @param loc1 Location 1
     * @param loc2 Location 2
     * @return The delta in the Y direction
     */
    public static double getDeltaY(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        return Math.abs(loc1.getY() - loc2.getY());
    }

    /**
     * Takes the given location and moves it to the highest Y
     * level.
     *
     * @param location The location
     * @return The location (Same instance as given) at highest
     * Y level.
     */
    public static Location toGround(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        int y = location.getWorld().getHighestBlockYAt(location);
        location.setY(y + 1);
        return location;
    }

    /**
     * Moves the location to the center of the block it is in.
     * Returns a new instance.
     *
     * @param location The location
     * @return New centered instance of location
     */
    public static Location centerBlock(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        double x = Math.floor(location.getX()) + 0.5;
        double y = Math.floor(location.getY()) + 0.5;
        double z = Math.floor(location.getZ()) + 0.5;
        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }

    /**
     * Moves the location to the center of the block it is in,
     * but with "feet on the ground". Returns a new instance.
     *
     * @param location The location
     * @return New centered instance of location
     */
    public static Location centerGround(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        double x = Math.floor(location.getX()) + 0.5;
        double y = Math.floor(location.getY());
        double z = Math.floor(location.getZ()) + 0.5;
        return new Location(location.getWorld(), x, y, z, location.getYaw(), location.getPitch());
    }

    /**
     * Parses the location into a string. Usable in configurations, and the lot.
     *
     * @param l - The location to parse.
     * @return - A string representing the key components of the location. To recreate it.
     */
    public static String parseLocation(Location l) {
        if (l == null) {
            return null;
        }
        return l.getWorld().getName() + ":" + l.getBlockX() + ":"
                + l.getBlockY() + ":" + l.getBlockZ() + ":" + l.getYaw() + ":" + l.getPitch();
    }

    /**
     * Parse the string into a location. Used after retrieving from configurations.
     *
     * @param s - The serialized location string.
     * @return - The location.
     */
    public static Location parseString(String s) {
        if (s == null) {
            return null;
        }

        String[] parts = s.split(":");
        String worldName = parts[0];
        String x = parts[1];
        String y = parts[2];
        String z = parts[3];
        String yaw = parts[4];
        String pitch = parts[5];
        return new Location(Bukkit.getWorld(worldName), Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), Float.parseFloat(yaw), Float.parseFloat(pitch));
    }

    /**
     * Takes a {@link JsonObject} containing a location and "extracts"
     * the location.
     *
     * @param object The {@link JsonObject}
     * @return The {@link Location} stored within the {@link JsonObject}
     */
    public static Location fromJson(JsonObject object) {
        if (object == null) {
            throw new IllegalArgumentException("JSON object can't be null!");
        }

        World world = Bukkit.getWorld(object.get("world").getAsString());
        double x = object.get("x").getAsDouble();
        double y = object.get("y").getAsDouble();
        double z = object.get("z").getAsDouble();
        float yaw = object.get("yaw").getAsFloat();
        float pitch = object.get("pitch").getAsFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Takes a {@link Location} and creates a {@link JsonObject} with
     * it's data.
     *
     * @param location The {@link Location}
     * @return The {@link JsonObject}
     */
    public static JsonObject toJson(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can't be null!");
        }

        String world = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        JsonObject object = new JsonObject();
        object.addProperty("world", world);
        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
        object.addProperty("yaw", yaw);
        object.addProperty("pitch", pitch);
        return object;
    }

    public static Location[] fromJson(JsonArray array) {
        Location[] locations = new Location[array.size()];

        for (int i = 0; i < locations.length; i++) {
            locations[i] = fromJson(array.get(i).getAsJsonObject());
        }

        return locations;
    }

    public static JsonArray toJson(Location... locations) {
        JsonArray array = new JsonArray();

        for (int i = 0; i < locations.length; i++) {
            array.add(toJson(locations[i]));
        }

        return array;
    }

    public static boolean isSamePosition(Location loc1, Location loc2) {
        return loc1.getX() == loc2.getX()
                && loc1.getY() == loc2.getY()
                && loc1.getZ() == loc2.getZ();
    }

    /**
     * Determines if two locations are in the same block
     *
     * @param loc1 Location 1
     * @param loc2 Location 2
     * @return Whether or not the locations are in the same block
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Iterates through every location between the two given points,
     * at an interval of 1 meter (Equal to 1 block). This method is
     * very similar to {@link #forEachMinToMax(Location, Location, Consumer)},
     * but instead of accepting the minimum and maximum points as
     * arguments, this method determines what the minimum and maximum
     * point is and the delegates to that method.
     *
     * @param loc1     A corner of the region to iterate through
     * @param loc2     The opposing corner of loc1
     * @param consumer The action to be executed for each location
     */
    public static void forEachBetween(Location loc1, Location loc2, Consumer<Location> consumer) {
        forEachMinToMax(getMinPoint(loc1, loc2), getMaxPoint(loc1, loc2), consumer);
    }

    public static Location getMinPoint(Location loc1, Location loc2) {
        double minX = Math.min(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        return new Location(loc1.getWorld(), minX, minY, minZ);
    }

    public static Location getMaxPoint(Location loc1, Location loc2) {
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double maxY = Math.max(loc1.getY(), loc2.getY());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());
        return new Location(loc1.getWorld(), maxX, maxY, maxZ);
    }

    /**
     * Iterates through every location between the two given points,
     * at an interval of 1 meter (Equal to 1 block).
     *
     * @param min      The minimum point
     * @param max      The maximum point
     * @param consumer The action to be executed for each location
     */
    public static void forEachMinToMax(Location min, Location max, Consumer<Location> consumer) {
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();

        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        World world = min.getWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location location = new Location(world, x, y, z);
                    consumer.accept(location);
                }
            }
        }
    }

    public static <T> T forEachWithResult(Location min, Location max, Function<Location, T> function) {
        return forEachWithResult(min, max, null, function);
    }

    /**
     * Iterates through every location between the two given points,
     * at an interval of 1 meter (Equal to 1 block). This method has
     * a goal, and a return value.
     *
     * @param min      The minimum point
     * @param max      The maximum point
     * @param def      The default value, if no result is found
     * @param function The function that checks the location. If the
     *                 desirable result isn't found, this function
     *                 should return {@code null}. If the result isn't
     *                 null, the loop is escaped and a value is returned.
     * @param <T>      The type of the return value
     * @return The found value, if none found, the default value will
     * be returned.
     */
    public static <T> T forEachWithResult(Location min, Location max, T def, Function<Location, T> function) {
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();

        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        World world = min.getWorld();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location location = new Location(world, x, y, z);
                    T result = function.apply(location);
                    if (result == null) {
                        continue;
                    }

                    return result;
                }
            }
        }

        return def;
    }

    public static boolean isBetween(Location location, Location min, Location max) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();

        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        return minX <= x && maxX >= x
                && minY <= y && maxY >= y
                && minZ <= z && maxZ >= z;
    }

    public static boolean hasLineOfSight(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }

        loc1 = UtilLocation.centerBlock(loc1.clone());
        loc2 = UtilLocation.centerBlock(loc2.clone());

        Vector vector = UtilVector.between(loc1, loc2).normalize();

        // Floor to exclude edges
        int dist = (int) Math.floor(loc1.distance(loc2));
        for (int i = 0; i < dist; i++) {
            loc1.add(vector);

            Block block = loc1.getBlock();
            if (UtilBlock.isTransparent(block)) {
                continue;
            }

            return false;
        }

        return true;
    }

    public static Location getHighestLocation(Location location) {
        return getHighestLocation(location, false);
    }

    public static Location getHighestLocation(Location location, boolean nullIfNone) {
        for (int y = 255; y >= 0; y--) {
            Location loc = new Location(location.getWorld(), location.getX(), y, location.getZ());
            if (loc.getBlock().getType() == Material.AIR
                    || loc.getBlock().getType() == Material.BARRIER) {
                continue;
            }

            return loc.add(0, 1, 0);
        }

        return nullIfNone ? null : new Location(location.getWorld(), location.getX(), 255, location.getZ());
    }

    public static List<Location> getCircle(Location center, double radius, int count) {
        double interval = 360.0 / count;
        List<Location> locations = Lists.newArrayList();

        for (int i = 0; i < count; i++) {
            double yaw = interval * i;
            Vector vector = UtilVector.getDirectionalVector(yaw, 0).multiply(radius);
            Location location = center.clone().add(vector);
            location.setYaw((float) (yaw - 180));
            location.setPitch(0.0f);

            locations.add(location);
        }

        return locations;
    }

    public static List<Location> getArchWithQuality(Location start, Location end, double quality) {
        Vector between = UtilVector.between(start, end);
        double radius = between.length() * 0.5;
        int count = (int) Math.round(quality * radius * Math.PI); // Kindergarten math yay
        return getArchWithCount(start, end, count);
    }

    public static List<Location> getArchWithCount(Location start, Location end, int count) {
        Vector between = UtilVector.between(start, end);
        Vector half = between.multiply(0.5);

        Location center = start.clone().add(half);

        double radius = half.length();
        double yaw = UtilVector.getYaw(between);

        return getArch(center, yaw, radius, count);
    }

    public static List<Location> getArch(Location center, double yaw, double radius, int count) {
        double interval = 180.0 / (count - 1); // 180 = half circle
        List<Location> locations = Lists.newLinkedList();

        for (int i = 0; i < count; i++) {
            Vector vector = UtilVector.getDirectionalVector(yaw, -180 + (i * interval)).multiply(radius);
            Location location = center.clone().add(vector);

            locations.add(location);
        }

        return locations;
    }

    public static List<Location> getLine(Location from, Location to, int count) {
        double low = count - 1; // We use this since we include the starting point
        Vector between = UtilVector.between(from, to).multiply(1.0 / low);

        List<Location> locations = Lists.newArrayList();

        for (int i = 0; i < count; i++) {
            Location location = from.clone().add(between.clone().multiply(i));
            locations.add(location);
        }

        return locations;
    }

    public static Location getRelative(Location location, int range) {
        Location spawn = null;

        int count = 0;

        while (spawn == null) {
            Vector vector = UtilVector.getRandomVector().multiply(ThreadLocalRandom.current().nextDouble(range));
            spawn = UtilLocation.getHighestLocation(location.clone().add(vector), true);
            if (++count <= 25) {
                continue;
            }

            return location;
        }

        return spawn.add(0.5, 1.0, 0.5);
    }

    public static Location getRelative(Location location, double rangeLow, double rangeHigh) {
        Location spawn = null;

        int count = 0;

        while (spawn == null) {
            Vector vector = UtilVector.getRandomVector().multiply(ThreadLocalRandom.current().nextDouble(rangeLow, rangeHigh));
            spawn = UtilLocation.getHighestLocation(location.clone().add(vector), true);
            if (++count <= 25) {
                continue;
            }

            return location;
        }

        return spawn.add(0.5, 1.0, 0.5);
    }
}
