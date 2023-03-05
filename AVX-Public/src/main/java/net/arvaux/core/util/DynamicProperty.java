package net.arvaux.core.util;

public class DynamicProperty {
    private String property;
    private String value;

    DynamicProperty(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
