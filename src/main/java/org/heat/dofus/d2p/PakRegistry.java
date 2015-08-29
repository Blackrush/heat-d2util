package org.heat.dofus.d2p;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Or D2P format as we know it. It is really a meta-format containing string-to-string key-value dict used as properties,
 * and indexes shared across multiple separated files.
 */
public class PakRegistry {
    private final Map<String, SubRegistry> subRegistries;

    public static class SubRegistry {
        private final Path path;
        private final Map<String, Index> indexes;
        private final Map<String, String> properties;

        public SubRegistry(Path path, Map<String, Index> indexes, Map<String, String> properties) {
            this.path = path;
            this.indexes = indexes;
            this.properties = properties;
        }

        public Path getPath() {
            return path;
        }

        public Map<String, Index> getIndexes() {
            return indexes;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        /*@Nullable*/
        public Index getIndex(String key) {
            return indexes.get(key);
        }

        /*@Nullable*/
        public String getProperty(String key) {
            return properties.get(key);
        }
    }

    public static class Index {
        private final Path path;
        private final long offset;
        private final int length;

        public Index(Path path, long offset, int length) {
            this.path = path;
            this.offset = offset;
            this.length = length;
        }

        public Path getPath() {
            return path;
        }

        public long getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }
    }

    public PakRegistry(Map<String, SubRegistry> subRegistries) {
        this.subRegistries = subRegistries;
    }

    public Map<String, SubRegistry> getSubRegistries() {
        return subRegistries;
    }

    public Index getIndex(String main, String sub) {
        SubRegistry reg = subRegistries.get(main);
        if (reg != null) {
            return reg.getIndex(sub);
        }
        return null;
    }

    public String getProperty(String main, String sub) {
        SubRegistry reg = subRegistries.get(main);
        if (reg != null) {
            return reg.getProperty(sub);
        }
        return null;
    }

    public Path getPath(String main) {
        SubRegistry reg = subRegistries.get(main);
        if (reg == null) {
            throw new NoSuchElementException();
        }
        return reg.path;
    }

    public Index findIndex(String key) {
        for (SubRegistry reg : subRegistries.values()) {
            Index index = reg.indexes.get(key);
            if (index != null) {
                return index;
            }
        }
        throw new NoSuchElementException();
    }

    public Index findIndexContaining(String key) {
        for (SubRegistry reg : subRegistries.values()) {
            for (Map.Entry<String, Index> entry : reg.indexes.entrySet()) {
                if (entry.getKey().contains(key)) {
                    return entry.getValue();
                }
            }
        }
        throw new NoSuchElementException();
    }

    public List<Index> getAllIndexes() {
        List<Index> indexes = new ArrayList<>();
        for (SubRegistry sub : subRegistries.values()) {
            indexes.addAll(sub.indexes.values());
        }
        return indexes;
    }
}
