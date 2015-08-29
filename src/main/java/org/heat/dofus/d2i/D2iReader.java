package org.heat.dofus.d2i;

import com.google.common.collect.ImmutableMap;
import org.behaviorismanaged.core.io.DataReader;
import org.fungsi.Throwables;
import org.heat.shared.io.HeapDataReader;
import org.heat.shared.io.IO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class D2iReader {
    private final Path base;
    private final Charset charset;

    public D2iReader(Path base, Charset charset) {
        this.charset = charset;
        this.base = requireNonNull(base, "base");
    }

    public D2iReader(Path base) {
        this(base, StandardCharsets.UTF_8);
    }

    public D2iModule read(String lang) {
        try (InputStream is = new FileInputStream(resolve(lang).toFile())) {
            byte[] bytes = IO.readAll(is::read);
            return read(lang, new HeapDataReader(bytes, 0, bytes.length));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public D2iModule lazyRead(String lang) {
        return new LazyD2iModule(lang, resolve(lang), charset);
    }

    private Path resolve(String lang) {
        return base.resolve(String.format("i18n_%s.d2i", lang));
    }

    private D2iModule read(String lang, DataReader reader) {
        Map<Long,   Long> indexes            = new HashMap<>();
        Map<Long,   Long> diacriticalIndexes = new HashMap<>();
        Map<String, Long> namedIndexes       = new HashMap<>();

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

        return new FatD2iModule(
                lang,
                map(reader, charset, indexes),
                map(reader, charset, namedIndexes),
                map(reader, charset, diacriticalIndexes)
        );
    }

    public static <Key> ImmutableMap<Key, String> map(DataReader reader, Charset charset, Map<Key, Long> indexes) {
        ImmutableMap.Builder<Key, String> res = ImmutableMap.builder();
        indexes.forEach((key, pointer) -> {
            reader.setPosition(pointer);
            int len = reader.readUInt16();
            String value = reader.readMultibytes(len, charset);
            res.put(key, value);
        });
        return res.build();
    }
}
