package org.heat.dofus.d2p;

import org.heat.dofus.Bytes;
import org.heat.dofus.Zlib;
import org.heat.dofus.d2p.maps.*;
import org.heat.shared.io.DataReader;
import org.heat.shared.io.HeapDataReader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class DlmReader {
    private DlmReader() {}

    public static final int CELL_HALF_WIDTH = 43;
    public static final float CELL_HALF_HEIGHT = 21.5f;

    /**
     * Parse the key that can decrypt map's data
     */
    public static byte[] parseKey(String lit) {
        return lit.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Compute the map's key used in PAK files
     * @param id the map's id
     * @return the non-null key
     */
    public static String computeMapKey(int id) {
        return (id % 10) + "/" + id + ".dlm";
    }

    /**
     * Read a map given its data.
     * @param mapSupplier a factory of map
     * @param key the key to decrypt the map data
     * @param pak the archive containing all possible maps
     * @param mapId the map's ID
     * @param <T> the map's type
     * @return a non-null map
     */
    public static <T extends DofusMap> T load(Supplier<T> mapSupplier, byte[] key, PakRegistry pak, int mapId) {
        return load(mapSupplier, key, pak.findIndex(computeMapKey(mapId)));
    }

    /**
     * Read a map given its data.
     * @param mapSupplier a factory of map
     * @param key the key to decrypt the map data
     * @param index where to find the map
     * @param <T> the map's type
     * @return a non-null map
     */
    public static <T extends DofusMap> T load(Supplier<T> mapSupplier, byte[] key, PakRegistry.Index index) {
        return load(mapSupplier, key, index.getPath(), index.getOffset(), index.getLength());
    }

    /**
     * Read a map given its data.
     * @param mapSupplier a factory of map
     * @param key the key to decrypt the map data
     * @param path where to find the map data
     * @param offset where the data starts in the file
     * @param length how much data to read to the file
     * @param <T> the map's type
     * @return a non-null map
     */
    public static <T extends DofusMap> T load(Supplier<T> mapSupplier, byte[] key, Path path, long offset, int length) {
        return load(mapSupplier, key, Bytes.from(path, offset, length));
    }

    /**
     * Read a map given its data.
     * @param mapSupplier a factory of map
     * @param key the key to decrypt the map data
     * @param bytes the data of the map
     * @param <T> the map's type
     * @return a non-null map
     */
    public static <T extends DofusMap> T load(Supplier<T> mapSupplier, byte[] key, byte[] bytes) {
        DataReader reader = new HeapDataReader(bytes, 0, bytes.length);

        byte header = reader.read_i8();
        if (header != 77) {
            byte[] uncompressed = Zlib.uncompress(bytes);
            DataReader tmp_reader = new HeapDataReader(uncompressed, 0, uncompressed.length);
            header = tmp_reader.read_i8();

            if (header != 77) {
                throw new InvalidFormatException();
            }

            reader = tmp_reader;
        }

        T map = mapSupplier.get();

        map.setVersion(reader.read_i8());
        map.setId(reader.read_ui32());

        if (map.getVersion() >= 7) {
            boolean isEncrypted = reader.read_bool();
            reader.read_i8();//encryptionVersion
            int encryptedLen = reader.read_i32();

            if (isEncrypted) {
                byte[] encrypted = reader.read_array_i8(encryptedLen);
                for (int i = 0; i < encrypted.length; i++) {
                    encrypted[i] ^= key[i % key.length];
                }
                reader = new HeapDataReader(encrypted, 0, encrypted.length);
            }
        }

        map.setRelativeId(reader.read_ui32());
        map.setMapType(reader.read_i8());
        map.setSubareaId(reader.read_i32());
        map.setTop(reader.read_i32());
        map.setBottom(reader.read_i32());
        map.setLeft(reader.read_i32());
        map.setRight(reader.read_i32());
        map.setShadowBonusOnEntities(reader.read_i32());
        if (map.getVersion() >= 3) {
            map.setBackgroundR(reader.read_i8());
            map.setBackgroundG(reader.read_i8());
            map.setBackgroundB(reader.read_i8());
        }
        if (map.getVersion() >= 4) {
            map.setZoomScale(reader.read_ui16() / 100);
            map.setZoomOffsetX(reader.read_i16());
            map.setZoomOffsetY(reader.read_i16());
        }
        map.setUseLowPassFilter(reader.read_bool());
        map.setUseReverb(reader.read_bool());
        map.setPresetId(map.isUseReverb() ? reader.read_i32() : -1);
        byte backgroundsCount = reader.read_i8();
        Fixture[] bgs = new Fixture[backgroundsCount];
        for (byte i = 0; i < backgroundsCount; i++) {
            Fixture bg = new Fixture();
            readFixture(reader, bg);
            bgs[i] = bg;
        }
        map.setBackground(bgs);
        byte foregroundsCount = reader.read_i8();
        Fixture[] fgs = new Fixture[foregroundsCount];
        for (byte i = 0; i < foregroundsCount; i++) {
            Fixture fg = new Fixture();
            readFixture(reader, fg);
            fgs[i] = fg;
        }
        map.setForeground(fgs);
        reader.read_i32(); // unused cell count field (even in the client, he's using constant)
        int cellsCount = 560;
        map.setGroundCRC(reader.read_i32());
        byte layersCount = reader.read_i8();
        Layer[] layers = new Layer[layersCount];
        for (byte i = 0; i < layersCount; i++) {
            Layer layer = readLayer(reader, map);
            layers[i] = layer;
        }
        map.setLayers(layers);
        DofusMapCell[] cells = new DofusMapCell[cellsCount];
        for (int i = 0; i < cellsCount; i++) {
            DofusMapCell cell = readMapCell(reader, map, i);
            cells[i] = cell;
        }
        map.setCells(cells);

        return map;
    }

    private static void readFixture(DataReader reader, Fixture f) {
        f.setId(reader.read_i32());
        f.setOffsetX(reader.read_i16());
        f.setOffsetY(reader.read_i16());
        f.setRotation(reader.read_i16());
        f.setXScale(reader.read_i16());
        f.setYScale(reader.read_i16());
        f.setRed(reader.read_i8());
        f.setGreen(reader.read_i8());
        f.setBlue(reader.read_i8());
        f.setAlpha(reader.read_ui8());
    }

    private static Layer readLayer(DataReader reader, DofusMap map) {
        Layer layer = new Layer();
        layer.setId(reader.read_i32());
        short layerCellsCount = reader.read_i16();
        LayerCell[] layerCells = new LayerCell[layerCellsCount];
        for (int j = 0; j < layerCellsCount; j++) {
            LayerCell layerCell = readLayerCell(reader, map);
            layerCells[j] = layerCell;
        }
        layer.setCells(layerCells);
        return layer;
    }

    private static LayerCell readLayerCell(DataReader reader, DofusMap map) {
        LayerCell layerCell = new LayerCell();
        layerCell.setId(reader.read_i16());
        short elementsCount = reader.read_i16();
        BasicElement[] elements = new BasicElement[elementsCount];
        for (int k = 0; k < elementsCount; k++) {
            byte elementType = reader.read_i8();
            BasicElement element = readLayerCellElement(reader, map, elementType);
            elements[k] = element;
        }
        layerCell.setElements(elements);
        return layerCell;
    }

    private static BasicElement readLayerCellElement(DataReader reader, DofusMap map, byte elementType) {
        switch (elementType) {
            case 2:
                return readGraphicalElement(reader, map);
            case 33:
                return readSoundElement(reader);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static BasicElement readSoundElement(DataReader reader) {
        SoundElement se = new SoundElement();
        se.setSoundId(reader.read_i32());
        se.setBaseVolume(reader.read_i16());
        se.setFullVolumeDistance(reader.read_i32());
        se.setNullVolumeDistance(reader.read_i32());
        se.setMinDelayBetweenLoops(reader.read_i16());
        se.setMaxDelayBetweenLoops(reader.read_i16());
        return se;
    }

    private static BasicElement readGraphicalElement(DataReader reader, DofusMap map) {
        GraphicalElement ge = new GraphicalElement();
        ge.setElementId(reader.read_ui32());
        //noinspection PointlessBitwiseExpression
        ge.setHue(reader.read_i8() | reader.read_i8() | reader.read_i8());
        //noinspection PointlessBitwiseExpression
        ge.setShadow(reader.read_i8() | reader.read_i8() | reader.read_i8());
        if (map.getVersion() <= 4) {
            ge.setOffsetX(reader.read_i8());
            ge.setOffsetY(reader.read_i8());
            ge.setPixelOffsetX((short) (ge.getOffsetX() * CELL_HALF_WIDTH));
            ge.setPixelOffsetY((short) (ge.getOffsetY() * CELL_HALF_HEIGHT));
        } else {
            ge.setPixelOffsetX(reader.read_i16());
            ge.setPixelOffsetY(reader.read_i16());
            ge.setOffsetX((short) (ge.getPixelOffsetX() / CELL_HALF_WIDTH));
            ge.setOffsetY((short) (ge.getPixelOffsetY() / CELL_HALF_HEIGHT));
        }
        ge.setAltitude(reader.read_i8());
        ge.setIdentifier(reader.read_ui32());
        return ge;
    }

    private static DofusMapCell readMapCell(DataReader reader, DofusMap map, int id) {
        DofusMapCell cell = new DofusMapCell(id);
        cell.setFloor((short) (reader.read_i8() * 10));
        cell.setLosmov(reader.read_i8()); // ui8 in the client
        cell.setSpeed(reader.read_i8());
        cell.setMapChangeData(reader.read_i8()); // ui8 in the client
        if (map.getVersion() > 5) {
            cell.setMoveZone(reader.read_i8()); // ui8 in the client
        }
        if (map.getVersion() > 7) {
            cell.setArrow(reader.read_i8());
        }
        return cell;
    }
}
