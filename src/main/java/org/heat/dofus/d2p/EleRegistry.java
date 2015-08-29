package org.heat.dofus.d2p;

import org.heat.dofus.d2p.elements.GraphicalElementData;

import java.util.Map;
import java.util.Set;

/**
 * A file format containing graphical element data.
 */
public class EleRegistry {
    private final int fileVersion;
    private final Map<Integer, GraphicalElementData> elements;
    private final Set<Integer> jpg;

    public EleRegistry(int fileVersion, Map<Integer, GraphicalElementData> elements, Set<Integer> jpg) {
        this.fileVersion = fileVersion;
        this.elements = elements;
        this.jpg = jpg;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public Map<Integer, GraphicalElementData> getElements() {
        return elements;
    }

    public Set<Integer> getJpg() {
        return jpg;
    }
}
