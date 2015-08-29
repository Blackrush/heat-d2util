package org.heat.dofus.d2p;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;

public class EleReaderTest {

    private Path dofusPath;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.load().resolve();
        this.dofusPath = Paths.get(config.getString("dofus.path"));
    }

    @Test
    public void testLoadElements() {
        // given
        Path elementsPath = dofusPath.resolve("elements.ele");

        // when
        EleRegistry reg = EleReader.read(elementsPath);

        // then
        assertNotNull("elements", reg.getElements());
        assertNotNull("jpg", reg.getJpg());
    }
}