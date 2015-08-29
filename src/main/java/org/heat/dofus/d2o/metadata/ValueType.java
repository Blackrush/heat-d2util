package org.heat.dofus.d2o.metadata;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

public enum ValueType {
    INT(-1),
    BOOLEAN(-2),
    STRING(-3),
    NUMBER(-4),
    I18N(-5),
    UINT(-6),
    VECTOR(-99),
    ;

    public final int id;

    ValueType(int id) { this.id = id; }

    public static Stream<ValueType> stream() { return Stream.of(values()); }

    public static ValueType valueOf(int id) {
        for (ValueType value : values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }
}
