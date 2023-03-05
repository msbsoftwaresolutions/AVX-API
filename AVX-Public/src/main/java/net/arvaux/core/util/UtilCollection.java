package net.arvaux.core.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.IntFunction;

public class UtilCollection {

    @SafeVarargs
    public static <T> String join(String prefix, String suffix, Function<T, String> stringFunction, T... values) {
        return join(prefix, suffix, true, false, stringFunction, values);
    }

    @SafeVarargs
    public static <T> String join(String prefix, String suffix, boolean startingPrefix, boolean endingSuffix, Function<T, String> stringFunction, T... values) {
        String[] strings = Arrays.stream(values).map(stringFunction).toArray(String[]::new);
        return join(prefix, suffix, startingPrefix, endingSuffix, strings);
    }

    public static String join(String prefix, String suffix, String... strings) {
        return join(prefix, suffix, true, false, strings);
    }

    public static String join(String prefix, String suffix, boolean startingPrefix, boolean endingSuffix, String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(prefix).append(string).append(suffix);
        }

        String completed = builder.toString();
        if (!startingPrefix) {
            completed = completed.substring(prefix.length());
        }

        if (!endingSuffix) {
            completed = completed.substring(0, completed.length() - suffix.length());
        }

        return completed;
    }

    public static <T> T[] combine(IntFunction<T[]> supplier, T[]... arrays) {
        int size = 0;
        for (T[] array : arrays) {
            size += array.length;
        }

        T[] combined = supplier.apply(size);
        int startIndex = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, combined, startIndex, array.length);
            startIndex += array.length;
        }

        return combined;
    }

    public static boolean contains(int checkFor, int... check) {
        for (int number : check) {
            if (number != checkFor) {
                continue;
            }

            return true;
        }

        return false;
    }

    @SafeVarargs
    public static <T> boolean contains(T object, T... check) {
        return Lists.newArrayList(check).contains(object);
    }

    public static <K, V> Map<K, V> sortByKey(Map<K, V> map, Comparator<K> comparator) {
        Map<K, V> linked = Maps.newLinkedHashMap();
        map.entrySet().stream().sorted(Entry.comparingByKey(comparator))
                .forEach(entry -> linked.put(entry.getKey(), entry.getValue()));
        return linked;
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map, Comparator<V> comparator) {
        Map<K, V> linked = Maps.newLinkedHashMap();
        map.entrySet().stream().sorted(Entry.comparingByValue(comparator))
                .forEach(entry -> linked.put(entry.getKey(), entry.getValue()));
        return linked;
    }

    public static <K, V> Map<K, V> sortByEntry(Map<K, V> map, Comparator<Entry<K, V>> comparator) {
        Map<K, V> linked = Maps.newLinkedHashMap();
        map.entrySet().stream().sorted(comparator).forEach(entry -> linked.put(entry.getKey(), entry.getValue()));
        return linked;
    }
}
