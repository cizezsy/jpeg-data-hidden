package me.cizezsy.jpeg.marker;

public class APP0Marker extends Marker {

    private int densityUnit;
    private int xDensity;
    private int yDensity;

    public APP0Marker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

    public int getDensityUnit() {
        return densityUnit;
    }

    public void setDensityUnit(int densityUnit) {
        this.densityUnit = densityUnit;
    }

    public int getxDensity() {
        return xDensity;
    }

    public void setxDensity(int xDensity) {
        this.xDensity = xDensity;
    }

    public int getyDensity() {
        return yDensity;
    }

    public void setyDensity(int yDensity) {
        this.yDensity = yDensity;
    }
}
