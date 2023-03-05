package net.arvaux.core.util;



import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DynamicString {
    private Component[] components;

    public DynamicString(Component[] components) {
        this.components = components;
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        return clazz.cast(Arrays.stream(this.components).filter(comp -> comp.getClass().equals(clazz)).findFirst().orElse(null));
    }

    public String toString() {
        return this.toString(new HashMap<>());
    }

    public String toString(Map<String, Object> definitions) {
        StringBuilder builder = new StringBuilder();
        for (Component component : this.components) {
            builder.append(component.getValue(definitions));
        }

        return builder.toString();
    }
}
