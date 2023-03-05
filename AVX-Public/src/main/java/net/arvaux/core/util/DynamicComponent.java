package net.arvaux.core.util;


import java.util.Arrays;

public abstract class DynamicComponent implements Component {
    private DynamicProperty[] properties;

    public DynamicComponent(DynamicProperty[] properties) {
        this.properties = properties;
    }

    public DynamicProperty[] getProperties() {
        return properties;
    }

    public DynamicProperty getProperty(String property) {
        return Arrays.stream(this.getProperties()).filter(prop -> prop.getProperty().equalsIgnoreCase(property)).findFirst().orElse(null);
    }

    @Override
    public void defineProperty(String identifier, String value) {
        this.getProperty(identifier).setValue(value);
    }
}
