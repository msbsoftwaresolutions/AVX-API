package net.arvaux.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicStringParser {
    private static Map<String, Class<? extends DynamicComponent>> componentMap = new HashMap<>();

    public static void registerComponent(String identifier, Class<? extends DynamicComponent> clazz) {
        if (identifier.length() < 3) {
            throw new IllegalArgumentException("Identifier too short");
        }

        componentMap.put(identifier, clazz);
    }

    public static DynamicString parse(String str) {
        List<Component> componentList = new ArrayList<>();

        char[] chars = str.toCharArray();
        int startIndex = -1, lastIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '{') {
                startIndex = i;
                i += 3;
            } else if (chars[i] == '}') {
                if (startIndex == -1) {
                    continue;
                }

                componentList.add(new ColoredStringComponent(str.substring(lastIndex, startIndex)));
                lastIndex = i + 1;

                String rawComponent = str.substring(startIndex, i + 1);
                String identifier = parseComponentIdentifier(rawComponent);

                DynamicProperty[] properties = parseProperties(rawComponent);
                DynamicComponent component = parseComponent(identifier, properties);
                if (component == null) {
                    throw new IllegalArgumentException("An error occurred while parsing " + identifier);
                }

                componentList.add(component);
            }
        }

        componentList.add(new ColoredStringComponent(str.substring(lastIndex, str.length())));
        return new DynamicString(componentList.toArray(new Component[componentList.size()]));
    }

    private static DynamicComponent parseComponent(String identifier, DynamicProperty[] properties) {
        if (!componentMap.containsKey(identifier)) {
            return null;
        }

        try {
            Class<? extends DynamicComponent> componentClass = componentMap.get(identifier);
            Constructor<? extends DynamicComponent> componentConstructor = componentClass.getConstructor(DynamicProperty[].class);
            return componentConstructor.newInstance(new Object[]{properties});
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DynamicProperty[] parseProperties(String str) {
        int startIndex = str.indexOf('[');
        int endIndex = str.indexOf(']');
        if (startIndex == -1 && endIndex == -1) {
            return new DynamicProperty[0];
        }

        if (startIndex == -1 || endIndex == -1) {
            throw new IllegalArgumentException("Missing '[' or ']'");
        }

        String[] strings = str.substring(startIndex + 1, endIndex).split(", ");
        DynamicProperty[] properties = new DynamicProperty[strings.length];
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            int equalsIndex = string.indexOf('=');
            properties[i] = new DynamicProperty(string.substring(0, equalsIndex), string.substring(equalsIndex + 1));
        }

        return properties;
    }

    private static String parseComponentIdentifier(String expression) {
        int endIndex = expression.indexOf('[');
        if (endIndex == -1) {
            endIndex = expression.length() - 1;
        }

        return expression.substring(1, endIndex);
    }
}
