package me.cizezsy.jpeg;

import me.cizezsy.jpeg.marker.*;

public class JPEGImage {

    private APP0Marker app0Marker;
    private DQTMarker dqtMarker;
    private SOF0Marker sof0Marker;
    private DHTMarker dhtMarker;
    private DRIMarker driMarker;
    private SOSMarker sosMarker;


    public APP0Marker getApp0Marker() {
        return app0Marker;
    }

    public void setApp0Marker(APP0Marker app0Marker) {
        this.app0Marker = app0Marker;
    }

    public DQTMarker getDqtMarker() {
        return dqtMarker;
    }

    public void setDqtMarker(DQTMarker dqtMarker) {
        this.dqtMarker = dqtMarker;
    }

    public SOF0Marker getSof0Marker() {
        return sof0Marker;
    }

    public void setSof0Marker(SOF0Marker sof0Marker) {
        this.sof0Marker = sof0Marker;
    }

    public DHTMarker getDhtMarker() {
        return dhtMarker;
    }

    public void setDhtMarker(DHTMarker dhtMarker) {
        this.dhtMarker = dhtMarker;
    }

    public DRIMarker getDriMarker() {
        return driMarker;
    }

    public void setDriMarker(DRIMarker driMarker) {
        this.driMarker = driMarker;
    }

    public SOSMarker getSosMarker() {
        return sosMarker;
    }

    public void setSosMarker(SOSMarker sosMarker) {
        this.sosMarker = sosMarker;
    }
}
