package org.heat.dofus.d2p.maps;

public class BasicElement {
    private byte type;

    public BasicElement() {
    }

    public BasicElement(byte type) {
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }
}
