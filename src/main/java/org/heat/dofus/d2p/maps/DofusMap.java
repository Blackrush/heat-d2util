package org.heat.dofus.d2p.maps;

import lombok.Data;

@Data
public class DofusMap {
    private byte version;
    private long id;
    private long relativeId;
    private byte mapType;
    private int subareaId;
    private int top;
    private int bottom;
    private int left;
    private int right;
    private int shadowBonusOnEntities;
    private byte backgroundR;
    private byte backgroundG;
    private byte backgroundB;
    private int zoomScale;
    private short zoomOffsetX;
    private short zoomOffsetY;
    private boolean useLowPassFilter;
    private boolean useReverb;
    private int presetId;
    private Fixture[] background, foreground;
    private int groundCRC;
    private Layer[] layers;
    private DofusMapCell[] cells;
}
