package org.heat.dofus.d2p.maps;

import lombok.Data;

@Data
public class Layer {
    private int id;
    private LayerCell[] cells;
}
