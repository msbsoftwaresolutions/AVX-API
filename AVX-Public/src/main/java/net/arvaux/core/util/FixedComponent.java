package net.arvaux.core.util;

import java.util.Map;

public class FixedComponent implements Component {

    private Object value;

    public FixedComponent(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue(Map<String, Object> definitions) {
        return this.value;
    }

    @Override
    public void defineProperty(String identifier, String value) {
        throw new UnsupportedOperationException("Can't modify fixed component");
    }
}
