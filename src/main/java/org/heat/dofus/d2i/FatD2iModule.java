package org.heat.dofus.d2i;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
final class FatD2iModule implements D2iModule {
    final String lang;

    final ImmutableMap<Long, String> byId;
    final ImmutableMap<String, String> byName;
    final ImmutableMap<Long, String> diacritical;

    @Override
    public String byId(long id) {
        String msg = byId.get(id);
        if (msg == null) {
            throw new NoSuchElementException();
        }
        return msg;
    }

    @Override
    public String byName(String name) {
        String msg = byName.get(name);
        if (msg == null) {
            throw new NoSuchElementException();
        }
        return msg;
    }

    @Override
    public String diacritical(long id) {
        String res = diacritical.get(id);
        if (res == null) {
            return byId(id);
        }
        return res;
    }

    @Override
    public LongStream keys() {
        return byId.keySet().stream().mapToLong(Long::longValue);
    }

    @Override
    public Stream<String> namedKeys() {
        return byName.keySet().stream();
    }

    @Override
    public boolean isDiacritical(long id) {
        return diacritical.containsKey(id);
    }

    @Override
    public boolean hasMessageId(long id) {
        return byId.containsKey(id);
    }

    @Override
    public boolean hasMessageName(String name) {
        return byName.containsKey(name);
    }
}
