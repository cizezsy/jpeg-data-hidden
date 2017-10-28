package me.cizezsy.jpeg.marker;

public class DRIMarker extends Marker{
    private int interval;

    public DRIMarker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
