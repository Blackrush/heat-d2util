package org.heat.dofus.d2p.maps;

public class SoundElement extends BasicElement {
    private int soundId;
    private short baseVolume;
    private int fullVolumeDistance;
    private int nullVolumeDistance;
    private short minDelayBetweenLoops, maxDelayBetweenLoops;

    public SoundElement() {
    }

    public SoundElement(byte type, int soundId, short baseVolume, int fullVolumeDistance, int nullVolumeDistance, short minDelayBetweenLoops, short maxDelayBetweenLoops) {
        super(type);
        this.soundId = soundId;
        this.baseVolume = baseVolume;
        this.fullVolumeDistance = fullVolumeDistance;
        this.nullVolumeDistance = nullVolumeDistance;
        this.minDelayBetweenLoops = minDelayBetweenLoops;
        this.maxDelayBetweenLoops = maxDelayBetweenLoops;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public short getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(short baseVolume) {
        this.baseVolume = baseVolume;
    }

    public int getFullVolumeDistance() {
        return fullVolumeDistance;
    }

    public void setFullVolumeDistance(int fullVolumeDistance) {
        this.fullVolumeDistance = fullVolumeDistance;
    }

    public int getNullVolumeDistance() {
        return nullVolumeDistance;
    }

    public void setNullVolumeDistance(int nullVolumeDistance) {
        this.nullVolumeDistance = nullVolumeDistance;
    }

    public short getMinDelayBetweenLoops() {
        return minDelayBetweenLoops;
    }

    public void setMinDelayBetweenLoops(short minDelayBetweenLoops) {
        this.minDelayBetweenLoops = minDelayBetweenLoops;
    }

    public short getMaxDelayBetweenLoops() {
        return maxDelayBetweenLoops;
    }

    public void setMaxDelayBetweenLoops(short maxDelayBetweenLoops) {
        this.maxDelayBetweenLoops = maxDelayBetweenLoops;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoundElement that = (SoundElement) o;

        if (baseVolume != that.baseVolume) return false;
        if (fullVolumeDistance != that.fullVolumeDistance) return false;
        if (maxDelayBetweenLoops != that.maxDelayBetweenLoops) return false;
        if (minDelayBetweenLoops != that.minDelayBetweenLoops) return false;
        if (nullVolumeDistance != that.nullVolumeDistance) return false;
        if (soundId != that.soundId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = soundId;
        result = 31 * result + (int) baseVolume;
        result = 31 * result + fullVolumeDistance;
        result = 31 * result + nullVolumeDistance;
        result = 31 * result + (int) minDelayBetweenLoops;
        result = 31 * result + (int) maxDelayBetweenLoops;
        return result;
    }

    @Override
    public String toString() {
        return "SoundElement(" +
                "soundId=" + soundId +
                ", baseVolume=" + baseVolume +
                ", fullVolumeDistance=" + fullVolumeDistance +
                ", nullVolumeDistance=" + nullVolumeDistance +
                ", minDelayBetweenLoops=" + minDelayBetweenLoops +
                ", maxDelayBetweenLoops=" + maxDelayBetweenLoops +
                ')';
    }
}
