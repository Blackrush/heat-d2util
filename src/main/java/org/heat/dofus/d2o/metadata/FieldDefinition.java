package org.heat.dofus.d2o.metadata;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;

import java.lang.reflect.Field;

@Value
@Wither
public class FieldDefinition {
    @NonNull String name;
    @NonNull ValueDefinition type;
    Field dataField;
}
