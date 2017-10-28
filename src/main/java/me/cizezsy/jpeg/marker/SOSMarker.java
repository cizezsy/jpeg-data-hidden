package me.cizezsy.jpeg.marker;

public class SOSMarker extends Marker {

    private int selectStart;
    private int selectEnd;
    private int select;

    public SOSMarker(Marker marker) {
        this.tag = marker.tag;
        this.position = marker.position;
        this.length = marker.length;
        this.stuffing = marker.stuffing;
    }

    public int getSelectStart() {
        return selectStart;
    }

    public void setSelectStart(int selectStart) {
        this.selectStart = selectStart;
    }

    public int getSelectEnd() {
        return selectEnd;
    }

    public void setSelectEnd(int selectEnd) {
        this.selectEnd = selectEnd;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }
}
