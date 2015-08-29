package org.heat.dofus.d2p.maps;

import lombok.Data;

@Data
public class LayerCell {
    private short id;
    private BasicElement[] elements;
}
