package org.heat.dofus.d2p.maps;

import lombok.Data;

@Data
public class Fixture {
    private int id;
    private short offsetX, offsetY;
    private short rotation;
    private short xScale, yScale;
    private byte red, green, blue;
    private short alpha;
}
