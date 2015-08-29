package org.heat.dofus.d2o;

public interface DataClassLookup {
    /**
     * Lookup the data class of a D2o object
     * @param name the name of the D2o object
     * @param pakage the package of the D2o object
     * @return a non-null class
     * @throws java.lang.ClassNotFoundException if the D2o object doesn't not have any Java class representation
     */
    Class<?> lookup(String name, String pakage) throws ClassNotFoundException;
}
