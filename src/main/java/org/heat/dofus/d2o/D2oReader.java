package org.heat.dofus.d2o;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import org.behaviorismanaged.core.io.DataReader;
import org.fungsi.Throwables;
import org.heat.dofus.d2o.metadata.*;
import org.heat.dofus.network.netty.NettyDataReader;
import org.heat.shared.stream.ImmutableCollectors;
import org.heat.shared.stream.Streams;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class D2oReader {
    public static final String D2O_HEADER = "D2O";
    public static final String ANKAMA_SIGNED_FILE_HEADER = "AKSF";
    public static final int D2O_HEADER_MODULE_LENGTH = 50;

    private final ByteBufAllocator alloc;
    private final DataClassLookup dataClasses;

    public D2oReader(ByteBufAllocator alloc, DataClassLookup dataClasses) {
        this.alloc = alloc;
        this.dataClasses = dataClasses;
    }

    private static String filename(Path p) {
        String filename = p.getFileName().toString();
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private static Field findField(Class<?> self, String name) {
        for (Class<?> it = self; it != Object.class; it = it.getSuperclass()) {
            try {
                Field field = it.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {

            }
        }
        return null;
    }

    /**
     * Load a module's metadata.
     * @param path the path where the module is located
     * @return a non-null module
     * @throws org.heat.dofus.d2o.MalformedD2oFileException if the module's metadata couldnt be read
     */
    public ModuleDefinition loadModule(Path path) {
        try (FileChannel ch = FileChannel.open(path)) {
            ch.position(0L);

            ByteBuf buf = alloc.buffer(D2O_HEADER_MODULE_LENGTH);
            try {
                buf.writeBytes(ch, D2O_HEADER_MODULE_LENGTH);

                DataReader reader = NettyDataReader.of(buf);

                // read header
                if (!reader.readMultibytes(3, US_ASCII).equalsIgnoreCase(D2O_HEADER)) {
                    reader.resetPosition();
                    if (!reader.readUTF().equalsIgnoreCase(ANKAMA_SIGNED_FILE_HEADER)) {
                        throw new MalformedD2oFileException();
                    }

                    short formatVersion = reader.readInt16();

                    long len = reader.readUInt32();
                    long contentOffset = reader.getPosition() + len;

                    if (!reader.readMultibytes(3, US_ASCII).equalsIgnoreCase(D2O_HEADER)) {
                        throw new MalformedD2oFileException();
                    }

                    buf.resetReaderIndex().resetWriterIndex();
                    buf.writeBytes(ch.position(contentOffset), D2O_HEADER_MODULE_LENGTH);

                    int ptr = reader.readInt32();
                    int bodyLength = Ints.checkedCast(ch.size()) - ptr;
                    ByteBuf bodyBuf = alloc.buffer(bodyLength);
                    try {
                        bodyBuf.writeBytes(ch.position(ptr), bodyLength);
                        return readModule(NettyDataReader.of(bodyBuf), path, contentOffset, formatVersion);
                    } finally {
                        bodyBuf.release();
                    }
                } else {
                    int ptr = reader.readInt32();
                    int bodyLength = Ints.checkedCast(ch.size()) - ptr;
                    ByteBuf bodyBuf = alloc.buffer(bodyLength);
                    try {
                        bodyBuf.writeBytes(ch.position(ptr), bodyLength);
                        return readModule(NettyDataReader.of(bodyBuf), path, 0, 0);
                    } finally {
                        bodyBuf.release();
                    }
                }
            } finally {
                buf.release();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Load all containing objects of a module, and associate them with their ID.
     * @param mod the module to load
     * @return a non-null map containing the module's objects with their ID as key
     */
    public Map<Integer, Object> loadMap(ModuleDefinition mod) {
        try (FileChannel ch = FileChannel.open(mod.getPath())) {
            int fileLength = Ints.checkedCast(ch.size());
            int bodyLength = fileLength - Ints.checkedCast(mod.getStartIndex());

            ByteBuf buf = alloc.buffer(bodyLength);
            try {
                buf.writeBytes(ch.position(mod.getStartIndex()), bodyLength);

                DataReader reader = NettyDataReader.of(buf);

                Map<Integer, Object> res = new HashMap<>();
                for (int id : mod.getIndexes().keySet()) {
                    res.put(id, readResolve(reader, mod));
                }

                return res;
            } finally {
                buf.release();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Load all containing objects of a module
     * @param mod the module to load
     * @return a non-null list containing the module's objects
     */
    public List<Object> load(ModuleDefinition mod) {
        try (FileChannel ch = FileChannel.open(mod.getPath())) {
            int fileLength = Ints.checkedCast(ch.size());
            int bodyLength = fileLength - Ints.checkedCast(mod.getStartIndex());

            ByteBuf buf = alloc.buffer(bodyLength);
            try {
                buf.writeBytes(ch.position(mod.getStartIndex()), bodyLength);

                DataReader reader = NettyDataReader.of(buf);

                return Streams.times(mod.getNbObjects())
                        .mapToObj(i -> readResolve(reader, mod))
                        .collect(Collectors.toList())
                        ;
            } finally {
                buf.release();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Load all containing objects of a module in a type-safe way
     * @param mod the module to load
     * @param dataClass the type of data contained in the module
     * @param <T> the type of data contained in the module
     * @return a non-null list containing the module's objects
     */
    @SuppressWarnings({"unchecked", "UnusedParameters"})
    public <T> List<T> load(ModuleDefinition mod, Class<T> dataClass) {
        // trust the user and spare wasteful stream computation
        return (List) load(mod);
    }

    /**
     * Load a single object from the module given its ID
     * @param mod the module where to find the object
     * @param id the object's ID
     * @return a non-null object contained in the module
     * @throws java.util.NoSuchElementException if the object couldn't be found
     */
    public Object load(ModuleDefinition mod, int id) {
        Integer pointer = mod.getIndexes().get(id);
        if (pointer == null) {
            throw new NoSuchElementException();
        }

        try (FileChannel ch = FileChannel.open(mod.getPath())) {
            int chunk = mod.getLargestChunk();
            ByteBuf buf = alloc.buffer(chunk);
            try {
                buf.writeBytes(ch.position(pointer), chunk);
                return readResolve(NettyDataReader.of(buf), mod);
            } finally {
                buf.release();
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Load a isngle object from the module given its ID in a type-safe way
     * @param mod the module where to find the object
     * @param id the object's ID
     * @param klass the type of the object
     * @param <T> the type of the object
     * @return a non-null object contained in the module
     * @throws java.util.NoSuchElementException if the object couldn't be found
     */
    @SuppressWarnings({"unchecked", "UnusedParameters"})
    public <T> T load(ModuleDefinition mod, int id, Class<T> klass) {
        return (T) load(mod, id);
    }

    ModuleDefinition readModule(DataReader reader, Path path, long contentOffset, int formatVersion) {
        // read indexes
        int indexesLength = reader.readInt32();
        Map<Integer, Integer> indexes = new HashMap<>();
        for (int i = 0; i < indexesLength; i += 8) {
            int key = reader.readInt32();
            int value = reader.readInt32();
            indexes.put(key, (int) (contentOffset + value));
        }

        // read classes
        int classesLength = reader.readInt32();
        Map<Integer, ClassDefinition> classes = new HashMap<>();
        for (int i = 0; i < classesLength; i++) {
            int id = reader.readInt32();
            ClassDefinition klass = readClass(reader, id);
            classes.put(id, klass);
        }

        return new ModuleDefinition(
                path,
                filename(path),
                formatVersion,
                contentOffset + 7,
                indexes,
                classes
        );
    }

    @SneakyThrows
    ClassDefinition readClass(DataReader reader, int id) {
        String name = reader.readUTF();
        String pakage = reader.readUTF();
        Class<?> dataClass = dataClasses.lookup(name, pakage);

        return new ClassDefinition(
                id,
                name,
                pakage,
                Streams.times(reader.readInt32())
                        .mapToObj(i -> {
                            String fieldName = reader.readUTF().replace("_", "");
                            return new FieldDefinition(fieldName, readValue(reader), findField(dataClass, fieldName));
                        })
                        .collect(ImmutableCollectors.toList()),
                dataClass
        );
    }
    
    ValueDefinition readValue(DataReader reader) {
        int id = reader.readInt32();
        ValueType tpe = ValueType.valueOf(id);
        if (tpe != null) {
            return readValueType(reader, tpe);
        }
        return new ValueDefinition.ClassValue(id);
    }

    ValueDefinition readValueType(DataReader reader, ValueType tpe) {
        switch (tpe) {
            case INT:
                return new ValueDefinition.IntValue();
            case BOOLEAN:
                return new ValueDefinition.BooleanValue();
            case STRING:
                return new ValueDefinition.StringValue();
            case NUMBER:
                return new ValueDefinition.NumberValue();
            case I18N:
                return new ValueDefinition.I18NValue();
            case UINT:
                return new ValueDefinition.UIntValue();
            case VECTOR:
                reader.readUTF(); // type name
                return new ValueDefinition.VectorValue(readValue(reader));
            default:
                throw new Error("should not happen");
        }
    }

    Object readResolve(DataReader reader, ModuleDefinition mod) {
        int classId = reader.readInt32();
        if (classId == -1431655766) {
            return null;
        }
        return read(reader, mod, mod.getClasses().get(classId));
    }

    Object readExact(DataReader reader, ModuleDefinition mod, ClassDefinition klass) {
        int classId = reader.readInt32();
        if (classId == -1431655766 || classId != klass.getId()) {
            return null;
        }
        return read(reader, mod, klass);
    }

    @SneakyThrows
    Object read(DataReader reader, ModuleDefinition mod, ClassDefinition klass) {
        FieldReader visitor = new FieldReader(this, reader, mod, klass);

        for (FieldDefinition def : klass.getFields()) {
            visitor.setField(def.getDataField());
            def.getType().accept(visitor);
        }

        return visitor.result();
    }

}
