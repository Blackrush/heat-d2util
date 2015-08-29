package org.heat.dofus.d2p.maps;

public class GraphicalElement extends BasicElement {
    private long elementId;
    private int hue;
    private int shadow;
    private short offsetX, offsetY;
    private short pixelOffsetX, pixelOffsetY;
    private byte altitude;
    private long identifier;

    public GraphicalElement() {
    }

    public GraphicalElement(byte type, long elementId, int hue, int shadow, short offsetX, short offsetY, short pixelOffsetX, short pixelOffsetY, byte altitude, long identifier) {
        super(type);
        this.elementId = elementId;
        this.hue = hue;
        this.shadow = shadow;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.pixelOffsetX = pixelOffsetX;
        this.pixelOffsetY = pixelOffsetY;
        this.altitude = altitude;
        this.identifier = identifier;
    }

    public long getElementId() {
        return elementId;
    }

    public void setElementId(long elementId) {
        this.elementId = elementId;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getShadow() {
        return shadow;
    }

    public void setShadow(int shadow) {
        this.shadow = shadow;
    }

    public short getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(short offsetX) {
        this.offsetX = offsetX;
    }

    public short getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(short offsetY) {
        this.offsetY = offsetY;
    }

    public short getPixelOffsetX() {
        return pixelOffsetX;
    }

    public void setPixelOffsetX(short pixelOffsetX) {
        this.pixelOffsetX = pixelOffsetX;
    }

    public short getPixelOffsetY() {
        return pixelOffsetY;
    }

    public void setPixelOffsetY(short pixelOffsetY) {
        this.pixelOffsetY = pixelOffsetY;
    }

    public byte getAltitude() {
        return altitude;
    }

    public void setAltitude(byte altitude) {
        this.altitude = altitude;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphicalElement that = (GraphicalElement) o;

        if (altitude != that.altitude) return false;
        if (elementId != that.elementId) return false;
        if (hue != that.hue) return false;
        if (identifier != that.identifier) return false;
        if (offsetX != that.offsetX) return false;
        if (offsetY != that.offsetY) return false;
        if (pixelOffsetX != that.pixelOffsetX) return false;
        if (pixelOffsetY != that.pixelOffsetY) return false;
        if (shadow != that.shadow) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (elementId ^ (elementId >>> 32));
        result = 31 * result + hue;
        result = 31 * result + shadow;
        result = 31 * result + (int) offsetX;
        result = 31 * result + (int) offsetY;
        result = 31 * result + (int) pixelOffsetX;
        result = 31 * result + (int) pixelOffsetY;
        result = 31 * result + (int) altitude;
        result = 31 * result + (int) (identifier ^ (identifier >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GraphicalElement(" +
                "elementId=" + elementId +
                ", hue=" + hue +
                ", shadow=" + shadow +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", pixelOffsetX=" + pixelOffsetX +
                ", pixelOffsetY=" + pixelOffsetY +
                ", altitude=" + altitude +
                ", identifier=" + identifier +
                ')';
    }
}
