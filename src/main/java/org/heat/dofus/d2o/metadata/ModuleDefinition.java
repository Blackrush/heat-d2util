package org.heat.dofus.d2o.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@ToString(of = {"name", "path", "largestChunk"})
public class ModuleDefinition {
    Path path;
    String name;
    int formatVersion;
    long startIndex;
    Map<Integer, Integer> indexes;
    Map<Integer, ClassDefinition> classes;
    int largestChunk;

    public ModuleDefinition(Path path, String name, int formatVersion, long startIndex, Map<Integer, Integer> indexes, Map<Integer, ClassDefinition> classes) {
        this.path = path;
        this.name = name;
        this.formatVersion = formatVersion;
        this.startIndex = startIndex;
        this.indexes = indexes;
        this.classes = classes;
        this.largestChunk = computeLargestChunk(indexes);
    }

    public Optional<ClassDefinition> findClass(String name) {
        return getClasses().values().stream().filter(it -> it.getName().equals(name)).findAny();
    }

    public Optional<ClassDefinition> findClass(int id) {
        return Optional.ofNullable(getClasses().get(id));
    }

    public Optional<ClassDefinition> findClass(Class<?> dataClass) {
        return getClasses().values().stream()
                .filter(x -> x.getDataClass().equals(dataClass))
                .findAny()
                ;
    }

    public Optional<Integer> findIndex(int id) {
        return Optional.ofNullable(getIndexes().get(id));
    }

    public int getNbObjects() {
        return indexes.size();
    }

    public static int computeLargestChunk(Map<Integer, Integer> map) {
        ArrayList<Integer> indexes = new ArrayList<>(map.size());
        indexes.addAll(map.values());
        Collections.sort(indexes);

        int largestChunk = 0;
        int last = -1;
        for (int ptr : indexes) {
            if (last == -1) {
                last = ptr;
            } else {
                int chunk = ptr - last;
                last = -1;
                if (largestChunk < chunk) {
                    largestChunk = chunk;
                }
            }
        }

        return largestChunk;
    }
}
