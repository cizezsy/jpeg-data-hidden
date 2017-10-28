package me.cizezsy.jpeg.marker;

public class Marker {
    protected int tag;
    protected int position;
    protected int length;
    protected int stuffing;

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getStuffing() {
        return stuffing;
    }

    public void setStuffing(int stuffing) {
        this.stuffing = stuffing;
    }
}
