package org.heat.dofus.d2p;

import com.google.common.primitives.Ints;
import org.heat.dofus.Bytes;
import org.heat.shared.io.DataReader;
import org.heat.shared.io.HeapDataReader;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class PakReader {
    private PakReader() {}

    /**
     * Read all indexes and properties of a PAK file
     * @param path where to find the file
     * @return a non-null registry
     */
    public static PakRegistry read(Path path) {
        Map<String, PakRegistry.SubRegistry> regs = new HashMap<>();
        Path dir = path.getParent();

        Path cur = path;
        while (true) {
            PakRegistry.SubRegistry reg = subread(cur);
            if (reg == null) {
                break;
            }
            regs.put(cur.getFileName().toString(), reg);

            String link = reg.getProperty("link");
            if (link != null) {
                cur = dir.resolve(link);
            } else {
                break;
            }
        }

        return new PakRegistry(regs);
    }


    private static PakRegistry.SubRegistry subread(Path path) {
        Map<String, String> properties = new HashMap<>();
        Map<String, PakRegistry.Index> indexes = new HashMap<>();

        // read the file content
        byte[] bytes;
        DataReader reader;

        // some data check I assume
        bytes = Bytes.from(path, 0, 2);
        reader = new HeapDataReader(bytes, 0, bytes.length);
        if (reader.read_ui8() != 2) { //vMax
            return null;
        }
        if (reader.read_ui8() != 1) { //vMin
            return null;
        }

        // seed at the end of the file to get our way through it
        bytes = Bytes.from(path, -24, 0);
        reader = new HeapDataReader(bytes, 0, bytes.length);
        long dataOffset = reader.read_ui32();
        reader.read_ui32();//dataCount
        long indexOffset = reader.read_ui32();
        long indexCount = reader.read_ui32();
        long propertiesOffset = reader.read_ui32();
        long propertiesCount = reader.read_ui32();

        // read properties
        bytes = Bytes.from(path, propertiesOffset, Ints.checkedCast(propertiesCount) * 200);
        reader = new HeapDataReader(bytes, 0, bytes.length);
        for (long i = 0; i < propertiesCount; i++) {
            String key = reader.read_str();
            String value = reader.read_str();
            properties.put(key, value);
        }

        // read indexes
        bytes = Bytes.from(path, indexOffset, Ints.checkedCast(indexCount) * 200);
        reader = new HeapDataReader(bytes, 0, bytes.length);
        for (long i = 0; i < indexCount; i++) {
            String filePath = reader.read_str();
            int fileOffset = reader.read_i32();
            int fileLength = reader.read_i32();
            indexes.put(filePath, new PakRegistry.Index(path, fileOffset + dataOffset, fileLength));
        }

        return new PakRegistry.SubRegistry(path, indexes, properties);
    }
}
