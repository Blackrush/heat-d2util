package org.heat.dofus.d2o.metadata;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Optional;

@Value
@Wither
public class ClassDefinition {
    int id;
    @NonNull String name;
    @NonNull String pakage;
    @NonNull ImmutableList<FieldDefinition> fields;
    @NonNull Class<?> dataClass;

    public Optional<FieldDefinition> findField(String name) {
        return fields.stream().filter(it -> it.getName().equals(name)).findAny();
    }
}
