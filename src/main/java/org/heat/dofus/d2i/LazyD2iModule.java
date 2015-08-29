package org.heat.dofus.d2i;

import lombok.Getter;
import lombok.SneakyThrows;
import org.behaviorismanaged.core.io.DataReader;
import org.fungsi.Throwables;
import org.fungsi.function.UnsafeFunction;
import org.heat.shared.io.ByteBufferReader;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class LazyD2iModule implements D2iModule {
    @Getter final String lang;
    final Path path;
    final Charset charset;

    final Map<Long,   Long> indexes            = new HashMap<>();
    final Map<Long,   Long> diacriticalIndexes = new HashMap<>();
    final Map<String, Long> namedIndexes       = new HashMap<>();

    final Map<Long,   String> cache            = new HashMap<>();
    final Map<Long,   String> diacriticalCache = new HashMap<>();
    final Map<String, String> namedCache       = new HashMap<>();

    LazyD2iModule(String lang, Path path, Charset charset) {
        this.lang = lang;
        this.path = path;
        this.charset = charset;

        useFile(this::loadIndexes);
    }

    <T> T openFile(UnsafeFunction<FileChannel, T> fn) {
        try (FileChannel ch = FileChannel.open(path)) {
            return fn.apply(ch);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    void useFile(Consumer<DataReader> fn) {
        openFile(ch -> {
            // unsafe, you just can't allocate +2Go (Integer.MAX_VALUE = 2,147,483,647)
            ByteBuffer buf = ByteBuffer.allocate((int) ch.size());
            ch.read(buf);
            buf.flip();
            fn.accept(ByteBufferReader.of(buf));

            return null;
        });
    }

    void loadIndexes(DataReader reader) {
        long indexesPointer = reader.readUInt32();
        reader.setPosition(indexesPointer);

        long indexesLength = reader.readUInt32();

        while (reader.getPosition() < indexesPointer + indexesLength) {
            long key = reader.readUInt32();
            boolean diacritical = reader.readBoolean();
            long pointer = reader.readUInt32();

            indexes.put(key, pointer);

            if (diacritical) {
                long newPointer = reader.readUInt32();
                diacriticalIndexes.put(key, newPointer);
            }
        }

        long textIndexesPointer = reader.getPosition();
        int textIndexesLength = reader.readInt32();

        while (reader.getPosition() < textIndexesPointer + textIndexesLength) {
            String key = reader.readUTF();
            long pointer = reader.readUInt32();
            namedIndexes.put(key, pointer);
        }
    }

    @SneakyThrows
    String readValueFromFile(FileChannel ch) {
        DataReader reader;
        int len;

        ByteBuffer lenBuf = ByteBuffer.allocate(2);
        ch.read(lenBuf);
        lenBuf.flip();

        reader = ByteBufferReader.of(lenBuf);
        len = reader.readUInt16();

        ByteBuffer valueBuf = ByteBuffer.allocate(len);
        ch.read(valueBuf);
        valueBuf.flip();

        reader = ByteBufferReader.of(valueBuf);
        return reader.readMultibytes(len, charset);
    }

    String loadValue(long pointer) {
        return openFile(ch -> {
            ch.position(pointer);
            return readValueFromFile(ch);
        });
    }

    @Override
    public String byId(long id) {
        String value = cache.get(id);
        if (value != null) {
            return value;
        }

        Long ptr = indexes.get(id);
        if (ptr == null) {
            throw new NoSuchElementException();
        }

        value = loadValue(ptr);
        cache.put(id, value);
        return value;
    }

    @Override
    public String byName(String name) {
        String value = namedCache.get(name);
        if (value != null) {
            return value;
        }

        Long ptr = namedIndexes.get(name);
        if (ptr == null) {
            throw new NoSuchElementException();
        }

        value = loadValue(ptr);
        namedCache.put(name, value);
        return value;
    }

    @Override
    public String diacritical(long id) {
        String value = diacriticalCache.get(id);
        if (value != null) {
            return value;
        }

        Long ptr = diacriticalIndexes.get(id);
        if (ptr == null) {
            throw new NoSuchElementException();
        }

        value = loadValue(ptr);
        diacriticalCache.put(id, value);
        return value;
    }

    @Override
    public LongStream keys() {
        return indexes.keySet().stream().mapToLong(Long::longValue);
    }

    @Override
    public Stream<String> namedKeys() {
        return namedIndexes.keySet().stream();
    }

    @Override
    public boolean isDiacritical(long id) {
        return diacriticalIndexes.containsKey(id);
    }

    @Override
    public boolean hasMessageId(long id) {
        return indexes.containsKey(id);
    }

    @Override
    public boolean hasMessageName(String name) {
        return namedIndexes.containsKey(name);
    }
}
