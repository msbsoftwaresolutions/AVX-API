package net.arvaux.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class helps with some mathematical calculations such as
 * the length between two locations etc and can provide threadsafe local
 * {@link ThreadLocalRandom Randoms}.
 */
public class UtilMath {

    public static final DecimalFormat FORMAT = new DecimalFormat("0.00");
    private static final TreeMap<Integer, String> ROMAN_TREE_MAP = Maps.newTreeMap();
    private static final Map<Integer, Double> SINE_MAP = Maps.newHashMap();
    private static final Map<Integer, Double> COSINE_MAP = Maps.newHashMap();

    static {
        ROMAN_TREE_MAP.put(1000, "M");
        ROMAN_TREE_MAP.put(900, "CM");
        ROMAN_TREE_MAP.put(500, "D");
        ROMAN_TREE_MAP.put(400, "CD");
        ROMAN_TREE_MAP.put(100, "C");
        ROMAN_TREE_MAP.put(90, "XC");
        ROMAN_TREE_MAP.put(50, "L");
        ROMAN_TREE_MAP.put(40, "XL");
        ROMAN_TREE_MAP.put(10, "X");
        ROMAN_TREE_MAP.put(9, "IX");
        ROMAN_TREE_MAP.put(5, "V");
        ROMAN_TREE_MAP.put(4, "IV");
        ROMAN_TREE_MAP.put(1, "I");
    }

    public static int toFixedPoint(double d) {
        return (int) (d * 32.0);
    }

    public static byte toPackedByte(float f) {
        return (byte) ((int) ((f * 256.0F) / 360.0F));
    }

    public static int squared(int value) {
        return value * value;
    }

    public static double squared(double value) {
        return value * value;
    }

    public static double cubed(double value) {
        return value * value * value;
    }

    /**
     * Generates a pseudo-random number using the given parameters
     *
     * @param min    The minimum value (inclusive)
     * @param max    The maximum value (exclusive)
     * @param weight The "weight" of this number. Increasing this
     *               makes the generator favour lower numbers within
     *               the given range. Lowering it does the opposite.
     * @return a pseudo-random number using the given parameters
     */
    public static int randomWeightedInt(int min, int max, double weight) {
        if (min >= max) {
            return min;
        }

        return (int) ((Math.pow(ThreadLocalRandom.current().nextDouble(), weight) * (max - min)) + min);
    }

    /**
     * Generates a pseudo-random number using the given parameters
     *
     * @param min    The minimum value (inclusive)
     * @param max    The maximum value (exclusive)
     * @param weight The "weight" of this number. Increasing this
     *               makes the generator favour lower numbers within
     *               the given range. Lowering it does the opposite.
     * @return a pseudo-random number using the given parameters
     */
    public static double randomWeightedDouble(double min, double max, double weight) {
        if (min >= max) {
            return min;
        }

        return (Math.pow(ThreadLocalRandom.current().nextDouble(), weight) * (max - min)) + min;
    }

    // Preferably run on async thread to avoid locking everything in case of failure
    public static int randomGaussian(int min, int max, double scale) {
        double value;
        while (true) {
            if (((value = Math.abs(ThreadLocalRandom.current().nextGaussian() / scale)) > 1.0)) {
                continue;
            }

            break;
        }

        return (int) ((value * (max - min)) + min);
    }

    public static String toRoman(int number) {
        int l = ROMAN_TREE_MAP.floorKey(number);
        if (number == l) {
            return ROMAN_TREE_MAP.get(number);
        }

        return ROMAN_TREE_MAP.get(l) + toRoman(number - l);
    }

    public static double fastSin(double degrees) {
        return fastSin((int) degrees);
    }

    /**
     * Faster sine method. This method is limited to integers, since it
     * caches all values for sine in a map. This will be more lightweight
     * in many cases where we can sacrifice some accuracy, such as the
     * particle patterns used in the cosmetics module.
     *
     * @param degrees The angle. This variable is altered to be between 0 - 359
     *                using the modulo function.
     * @return A sine value corresponding to the given angle.
     */
    public static double fastSin(int degrees) {
        return SINE_MAP.computeIfAbsent(Math.floorMod(degrees, 360), value -> Math.sin(Math.toRadians(value)));
    }

    public static double fastCos(double degrees) {
        return fastCos((int) degrees);
    }

    /**
     * Faster cosine method. This method is limited to integers, since it
     * caches all values for cosine in a map. This will be more lightweight
     * in many cases where we can sacrifice some accuracy, such as the
     * particle patterns used in the cosmetics module.
     *
     * @param degrees The angle. This variable is altered to be between 0 - 359
     *                using the modulo function.
     * @return A cosine value corresponding to the given angle.
     */
    public static double fastCos(int degrees) {
        return COSINE_MAP.computeIfAbsent(Math.floorMod(degrees, 360), value -> Math.cos(Math.toRadians(value)));
    }

    public static double trim(int degree, double d) {
        StringBuilder format = new StringBuilder("0.0");

        for (int i = 1; i < degree; i++) {
            format.append("0");
        }

        DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
        DecimalFormat twoDForm = new DecimalFormat(format.toString(), symb);
        return Double.valueOf(twoDForm.format(d));
    }

    public static int r(int i) {
        return ThreadLocalRandom.current().nextInt(i);
    }

    public static int r() {
        return ThreadLocalRandom.current().nextInt();
    }

    public static double offset2d(Entity a, Entity b) {
        return offset2d(a.getLocation().toVector(), b.getLocation().toVector());
    }

    public static double offset2d(Location a, Location b) {
        return offset2d(a.toVector(), b.toVector());
    }

    public static double offset2d(Vector a, Vector b) {
        a.setY(0);
        b.setY(0);
        return a.subtract(b).length();
    }

    public static double offset(Entity a, Entity b) {
        return offset(a.getLocation().toVector(), b.getLocation().toVector());
    }

    public static double offset(Location a, Location b) {
        return offset(a.toVector(), b.toVector());
    }

    public static double offset(Vector a, Vector b) {
        return a.subtract(b).length();
    }

    public static double offsetSquared(Entity a, Entity b) {
        return offsetSquared(a.getLocation(), b.getLocation());
    }

    public static double offsetSquared(Location a, Location b) {
        return offsetSquared(a.toVector(), b.toVector());
    }

    public static double offsetSquared(Vector a, Vector b) {
        return a.distanceSquared(b);
    }

    public static double rr(double d, boolean bidirectional) {
        if (bidirectional) {
            return Math.random() * (2 * d) - d;
        }

        return Math.random() * d;
    }

    public static <T> T getRandomAndRemove(List<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }

        int index = ThreadLocalRandom.current().nextInt(list.size());
        return list.remove(index);

    }

    @SafeVarargs
    public static <T> T randomElement(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }

        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    public static <K, V> Entry<K, V> randomEntry(Map<K, V> map) {
        return map.entrySet().stream().skip(ThreadLocalRandom.current().nextInt(map.size())).findFirst().orElse(null);
    }

    public static <T> T randomElement(Collection<T> collection) {
        if (collection == null || collection.size() == 0) {
            return null;
        }

        if (collection instanceof List) {
            return ((List<T>) collection).get(ThreadLocalRandom.current().nextInt(collection.size()));
        }

        return collection.stream().skip(ThreadLocalRandom.current().nextInt(collection.size())).findFirst().orElse(null);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static long clamp(long num, long min, long max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : (num > max ? max : num);
    }

    public static List<Location> circle(Location loc, int r, int h, boolean hollow, boolean sphere, int plusY) {
        List<Location> blocks = Lists.newArrayList();

        int cx = loc.getBlockX();
        int cy = loc.getBlockY();
        int cz = loc.getBlockZ();

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        Location l = new Location(loc.getWorld(), x, y + plusY, z);
                        blocks.add(l);
                    }
                }
            }
        }

        return blocks;
    }

    public static boolean doAction(int frequency) {
        return r(frequency) == 0;
    }

    public static boolean doAction(long frequency) {
        return ThreadLocalRandom.current().nextLong(frequency) == 0;
    }
}

