package org.heat.dofus.d2p.maps;

import lombok.Data;

@Data
public class DofusMapCell {
    private final int id;
    private short floor;
    private byte losmov;
    private byte speed;
    private byte mapChangeData;
    private byte moveZone;
    private byte arrow;
}
