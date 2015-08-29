package org.heat.dofus.d2o;

import com.ankamagames.dofus.datacenter.abuse.AbuseReasons;
import com.ankamagames.dofus.datacenter.breeds.Head;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.netty.buffer.PooledByteBufAllocator;
import org.heat.dofus.d2o.metadata.ClassDefinition;
import org.heat.dofus.d2o.metadata.FieldDefinition;
import org.heat.dofus.d2o.metadata.ModuleDefinition;
import org.heat.dofus.d2o.metadata.ValueDefinition;
import org.heat.shared.Stopwatch;
import org.heat.shared.stream.ImmutableCollectors;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.heat.shared.tests.CollectionMatchers.hasSize;
import static org.heat.shared.tests.CollectionMatchers.notEmpty;
import static org.junit.Assert.assertFalse;

public class D2oReaderTest {
    private static final Logger log = LoggerFactory.getLogger(D2oReaderTest.class);

    private Path dofusCommonDataDir;
    private D2oReader reader;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.load();

        Path dofusDir = Paths.get(config.getString("dofus.path"));

        dofusCommonDataDir = dofusDir.resolve("data/common");
        reader = new D2oReader(new PooledByteBufAllocator(true), HeatDataClassLookup.INSTANCE);
    }

    @Test
    public void testLoadModule() throws Exception {
        // given
        Path path = dofusCommonDataDir.resolve("AbuseReasons.d2o");

        // when
        ModuleDefinition mod = reader.loadModule(path);

        // then
        assertThat(mod.getPath(), is(path));
        assertThat(mod.getName(), is("AbuseReasons"));
        assertThat(mod.getClasses(), hasSize(1));

        ClassDefinition klass = mod.getClasses().values().stream().findAny().get();
        assertThat(klass.getId(), is(1));
        assertThat(klass.getName(), is("AbuseReasons"));
        assertThat(klass.getFields(), hasSize(3));
        assertThat(klass.getDataClass(), equalTo(AbuseReasons.class));

        Iterator<FieldDefinition> it = klass.getFields().iterator();
        FieldDefinition field;

        field = it.next();
        assertThat(field.getName(), is("abuseReasonId"));
        assertThat(field.getType(), instanceOf(ValueDefinition.IntValue.class));
        field = it.next();
        assertThat(field.getName(), is("mask"));
        assertThat(field.getType(), instanceOf(ValueDefinition.IntValue.class));
        field = it.next();
        assertThat(field.getName(), is("reasonTextId"));
        assertThat(field.getType(), instanceOf(ValueDefinition.I18NValue.class));

        assertFalse(it.hasNext());
    }

    @Test
    public void testLoad() throws Exception {
        // given
        Path path = dofusCommonDataDir.resolve("AbuseReasons.d2o");
        Path headsPath = dofusCommonDataDir.resolve("Heads.d2o");

        // when
        ModuleDefinition mod = reader.loadModule(path);
        ModuleDefinition headsMod = reader.loadModule(headsPath);
        List<AbuseReasons> res = reader.load(mod, AbuseReasons.class);
        List<Head> heads = reader.load(headsMod, Head.class);

        // then
        assertThat(res, hasSize(6));
        assertThat(heads, hasSize(240));
    }

    @Test
    public void testLoadOne() throws Exception {
        // given
        Path path = dofusCommonDataDir.resolve("Heads.d2o");

        // when
        ModuleDefinition mod = reader.loadModule(path);
        Head head = reader.load(mod, 1, Head.class);

        // then
        assertThat(head.getId(), is(1));
        assertThat(head.getBreed(), is(1));
    }

    @Test
    public void testLoadMap() throws Exception {
        // given
        Path path = dofusCommonDataDir.resolve("Heads.d2o");

        // when
        ModuleDefinition mod = reader.loadModule(path);
        Map<Integer, Object> res = reader.loadMap(mod);

        // then
        res.forEach((id, o) -> {
            assertThat(o, instanceOf(Head.class));
            assertThat(((Head) o).getId(), is(id));
        });
    }

    @Test
    public void testLoadAllModules() throws Exception {
        // given

        // when
        List<ModuleDefinition> mods =
                Files.list(dofusCommonDataDir)
                        .filter(x -> x.getFileName().toString().endsWith(".d2o"))
                        .map(reader::loadModule)
                        .collect(ImmutableCollectors.toList())
                ;

        List<Object> values = new ArrayList<>();
        Stopwatch sw = Stopwatch.system();
        for (ModuleDefinition mod : mods) {
            final int loaded;
            try (Stopwatch.H ignored = sw.start()) {
                List<Object> res = reader.load(mod);
                values.addAll(res);
                loaded = res.size();
            }
            log.debug(String.format("%25s %6d ms [%07d]", mod.getName(), sw.elapsed().toMillis(), loaded));
        }

        // then
        assertThat(mods, notEmpty());
        assertThat(values, notEmpty());
    }
}
