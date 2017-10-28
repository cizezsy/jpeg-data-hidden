package me.cizezsy.jpeg.marker;

public class ImageMarker extends Marker {

    public ImageMarker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

}
