package me.cizezsy.jpeg.marker;

public class DHTMarker extends Marker {

    public DHTMarker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

}
