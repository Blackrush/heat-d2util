package org.heat.dofus.d2o;

/**
 * The default D2o object data class lookup.
 */
public enum HeatDataClassLookup implements DataClassLookup {
    INSTANCE;

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> lookup(String name, String pakage) throws ClassNotFoundException {
        return Class.forName(computeDataClassName(pakage, name));
    }

    private static String computeDataClassName(String pkg, String name) {
        return pkg + "." + name;
    }
}
