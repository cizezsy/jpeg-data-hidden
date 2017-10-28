package me.cizezsy.jpeg.marker;

public class SOF0Marker extends Marker {

    private int precision;
    private int width;
    private int height;

    public SOF0Marker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }


    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
