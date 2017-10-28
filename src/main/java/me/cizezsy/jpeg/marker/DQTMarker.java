package me.cizezsy.jpeg.marker;

public class DQTMarker extends Marker {


    public DQTMarker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

}
