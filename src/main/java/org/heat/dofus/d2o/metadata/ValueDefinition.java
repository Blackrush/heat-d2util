package org.heat.dofus.d2o.metadata;

import lombok.EqualsAndHashCode;
import lombok.Value;

public abstract class ValueDefinition {
    public abstract int getId();
    public abstract <T> T accept(Visitor<T> visitor);

    public static final class IntValue extends ValueDefinition {
        @Override
        public int getId() {
            return -1;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitInt();
        }
    }

    public static final class BooleanValue extends ValueDefinition {
        @Override
        public int getId() {
            return -2;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitBoolean();
        }
    }

    public static final class StringValue extends ValueDefinition {
        @Override
        public int getId() {
            return -3;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitString();
        }
    }

    public static final class NumberValue extends ValueDefinition {
        @Override
        public int getId() {
            return -4;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitNumber();
        }
    }

    public static final class I18NValue extends ValueDefinition {
        @Override
        public int getId() {
            return -5;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitI18N();
        }
    }

    public static final class UIntValue extends ValueDefinition {
        @Override
        public int getId() {
            return -6;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitUInt();
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static final class VectorValue extends ValueDefinition {
        ValueDefinition component;

        @Override
        public int getId() { return -99; }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitVector(component);
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    public static final class ClassValue extends ValueDefinition {
        int id;

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visitClass(id);
        }
    }

    public static interface Visitor<T> {
        T visitInt();
        T visitBoolean();
        T visitString();
        T visitNumber();
        T visitI18N();
        T visitUInt();
        T visitVector(ValueDefinition component);
        T visitClass(int id);
    }
}
