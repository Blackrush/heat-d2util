package org.heat.dofus.d2p;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;

public class PakReaderTest {

    private Path dofusPath;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.load().resolve();
        dofusPath = Paths.get(config.getString("dofus.path"));
    }

    @Test
    public void testRead() throws Exception {
        // given
        Path mapsPath = dofusPath.resolve("maps0.d2p");

        // when
        PakRegistry reg = PakReader.read(mapsPath);

        // then
        assertNotNull("sub registries", reg.getSubRegistries());
        for (PakRegistry.SubRegistry sub : reg.getSubRegistries().values()) {
            assertNotNull("path", sub.getPath());
            assertNotNull(sub.getPath() + " indexes", sub.getIndexes());
            assertNotNull(sub.getPath() + " properties", sub.getProperties());
        }
    }
}