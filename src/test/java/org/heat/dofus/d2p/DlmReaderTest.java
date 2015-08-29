package org.heat.dofus.d2p;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.heat.dofus.Bytes;
import org.heat.dofus.d2p.maps.DofusMap;
import org.heat.shared.Stopwatch;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DlmReaderTest {

    private byte[] key;
    private PakRegistry pak;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.load().resolve();
        Path dofusPath = Paths.get(config.getString("dofus.path"));
        String mapsKey = config.getString("dofus.maps-key");

        Path mapsPath = dofusPath.resolve("maps0.d2p");
        pak = PakReader.read(mapsPath);

        key = DlmReader.parseKey(mapsKey);
    }

    @Test
    public void testLoad() throws Exception {
        // given
        int mapId = 139275;
        PakRegistry.Index index = pak.findIndexContaining(Integer.toString(mapId));

        // when
        DofusMap map = DlmReader.load(DofusMap::new, key, index);
        DofusMap map2 = DlmReader.load(DofusMap::new, key, pak, mapId);

        // then
        assertEquals("maps", map, map2);
        assertNotNull("map", map);
        assertEquals("map id", mapId, map.getId());
        assertNotNull("map background", map.getBackground());
        assertNotNull("map foreground", map.getForeground());
        assertNotNull("map layers", map.getLayers());
        assertNotNull("map cells", map.getCells());
    }

    @Test
    @Ignore("benchmark")
    public void testLoadAll() throws Exception {
        Stopwatch sw = Stopwatch.system();
        try (Stopwatch.H ignored = sw.start()) {
            for (PakRegistry.SubRegistry reg : pak.getSubRegistries().values()) {
                byte[] bytes = Bytes.from(reg.getPath());
                for (PakRegistry.Index index : reg.getIndexes().values()) {
                    byte[] slice = new byte[index.getLength()];
                    System.arraycopy(bytes, (int) index.getOffset(), slice, 0, index.getLength());
                    DlmReader.load(DofusMap::new, key, slice);
                }
            }
        }
        System.out.println(sw.elapsed());
    }
}