package org.heat.dofus.d2o;

import lombok.SneakyThrows;
import org.behaviorismanaged.core.io.DataReader;
import org.heat.dofus.d2o.metadata.ClassDefinition;
import org.heat.dofus.d2o.metadata.ModuleDefinition;
import org.heat.dofus.d2o.metadata.ValueDefinition;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import static com.google.common.base.Throwables.propagate;

class FieldReader implements ValueDefinition.Visitor<Object> {
    private static sun.misc.Unsafe U;
    static {
        try {
            java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            U = (sun.misc.Unsafe) field.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
    private final D2oReader self;
    private final DataReader reader;
    private final ModuleDefinition mod;

    private Object acc;
    private long offset;

    @SneakyThrows
    FieldReader(D2oReader self, DataReader reader, ModuleDefinition mod, ClassDefinition klass) {
        this.self = self;
        this.reader = reader;
        this.mod = mod;
        this.acc = U.allocateInstance(klass.getDataClass());
    }

    public void setField(Field field) {
        offset = U.objectFieldOffset(field);
    }

    public Object result() {
        return acc;
    }

    @Override
    public Void visitInt() {
        U.putInt(acc, offset, reader.readInt32());
        return null;
    }

    @Override
    public Void visitBoolean() {
        U.putBoolean(acc, offset, reader.readBoolean());
        return null;
    }

    @Override
    public Void visitString() {
        U.putObject(acc, offset, reader.readUTF());
        return null;
    }

    @Override
    public Void visitNumber() {
        U.putDouble(acc, offset, reader.readDouble());
        return null;
    }

    @Override
    public Void visitI18N() {
        U.putLong(acc, offset, reader.readUInt32());
        return null;
    }

    @Override
    public Void visitUInt() {
        U.putLong(acc, offset, reader.readUInt32());
        return null;
    }

    @Override
    public Void visitVector(ValueDefinition component) {
        Object oldAcc = acc;
        long oldOffset = offset;
        try {
            Class<?> componentClass = classOf(mod, component);
            Class<?> arrayClass = arrayClassOf(componentClass);

            int len = reader.readInt32();
            acc = Array.newInstance(componentClass, len);

            offset = U.arrayBaseOffset(arrayClass);
            int scale = U.arrayIndexScale(arrayClass);

            for (int i = 0; i < len; i++) {
                component.accept(this);
                offset += scale;
            }

            U.putObject(oldAcc, oldOffset, acc);
        } finally {
            offset = oldOffset;
            acc = oldAcc;
        }

        return null;
    }

    @Override
    public Void visitClass(int id) {
        U.putObject(acc, offset, self.readResolve(reader, mod));
        return null;
    }

    public static Class<?> classOf(ModuleDefinition mod, ValueDefinition def) {
        return def.accept(new ValueDefinition.Visitor<Class<?>>() {
            @Override
            public Class<?> visitInt() {
                return int.class;
            }

            @Override
            public Class<?> visitBoolean() {
                return boolean.class;
            }

            @Override
            public Class<?> visitString() {
                return String.class;
            }

            @Override
            public Class<?> visitNumber() {
                return double.class;
            }

            @Override
            public Class<?> visitI18N() {
                return long.class;
            }

            @Override
            public Class<?> visitUInt() {
                return long.class;
            }

            @Override
            public Class<?> visitVector(ValueDefinition component) {
                return arrayClassOf(component.accept(this));
            }

            @Override
            public Class<?> visitClass(int id) {
//                return mod.findClass(id)
//                        .map(ClassDefinition::getDataClass)
//                        .get()
//                        ;
                return mod.getClasses().get(id).getDataClass();
            }
        });
    }

    public static Class<?> arrayClassOf(Class<?> klass) {
        try {
            if (klass.isArray()) {
                return Class.forName("[" + klass.getName());
            } else {
                return Class.forName("[" + properClassName(klass));
            }
        } catch (ClassNotFoundException e) {
            throw propagate(e);
        }
    }

    public static String properClassName(Class<?> klass) {
        if (klass.isPrimitive()) {
            if (klass == byte.class) {
                return "B";
            } else if (klass == short.class) {
                return "S";
            } else if (klass == int.class) {
                return "I";
            } else if (klass == long.class) {
                return "J";
            } else if (klass == boolean.class) {
                return "Z";
            } else if (klass == char.class) {
                return "C";
            } else if (klass == float.class) {
                return "F";
            } else if (klass == double.class) {
                return "D";
            }
            throw new Error();
        } else {
            return "L" + klass.getName() + ";";
        }
    }

    public static Object newArray(ModuleDefinition mod, ValueDefinition def, int len) {
        return Array.newInstance(classOf(mod, def), len);
    }
}
