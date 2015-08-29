package org.heat.dofus.d2i;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class D2iReaderTest {

    private D2iReader reader;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.load();
        Path base = Paths.get(config.getString("dofus.path")).resolve(Paths.get("data", "i18n"));

        reader = new D2iReader(base);
    }

    @Test
    public void testRead() throws Exception {
        // given
        String lang = "en";

        // when
        D2iModule mod = reader.read(lang);

        // then
        long keysCount = mod.keys().count(),
             distinctKeysCount = mod.keys().distinct().count();

        assertTrue("mod not empty", keysCount > 0);
        assertEquals("duplicated keys", distinctKeysCount, keysCount);

        long namedKeysCount = mod.namedKeys().count(),
             distinctNamedKeysCount = mod.namedKeys().distinct().count();

        assertTrue("mod not empty", namedKeysCount > 0);
        assertEquals("duplicated named keys", distinctNamedKeysCount, namedKeysCount);
    }
}