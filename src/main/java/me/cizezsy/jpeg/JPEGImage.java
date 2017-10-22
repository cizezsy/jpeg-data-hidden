package me.cizezsy.jpeg;


import me.cizezsy.JPEGImageDecoder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JPEGImage {

    private Tag soi;
    private APP0 app0;
    private List<Tag> appn = new ArrayList<>();
    private List<DQT> dqt = new ArrayList<>();
    private SOF0 sof0;
    private List<DHT> dht = new ArrayList<>();
    private DRI dri;
    private SOS sos;
    private ImageData imageData;
    private Tag eoi;
    private List<ColorComponent> colorComponent = new ArrayList<>();

    //tag that doesn't be process  store in there
    //also make sure their order identical with origin
    private List<Tag> allTags = new ArrayList<>();
    private int[] unsignedByteData;

    public JPEGImage() {

    }

    public ColorComponent getOrCreateColorComponent(int id) {
        Optional<ColorComponent> colorComponentInfoOption =
                this.colorComponent.stream().filter(c -> c.getColorId() == id).findAny();

        ColorComponent colorComponent = colorComponentInfoOption.orElse(new ColorComponent());
        colorComponent.setColorId(id);
        return colorComponent;
    }

    public void setUnsignedByteData(int[] unsignedByteData) {
        this.unsignedByteData = unsignedByteData;
    }

    public int[] getUnsignedByteData() {
        return unsignedByteData;
    }

    public Tag getSoi() {
        return soi;
    }

    public void setSoi(Tag soi) {
        this.soi = soi;
    }

    public APP0 getApp0() {
        return app0;
    }

    public void setApp0(APP0 app0) {
        this.app0 = app0;
    }

    public List<Tag> getAppn() {
        return appn;
    }

    public void setAppn(List<Tag> appn) {
        this.appn = appn;
    }

    public List<DQT> getDqt() {
        return dqt;
    }

    public void setDqt(List<DQT> dqt) {
        this.dqt = dqt;
    }

    public SOF0 getSof0() {
        return sof0;
    }

    public void setSof0(SOF0 sof0) {
        this.sof0 = sof0;
    }

    public List<DHT> getDht() {
        return dht;
    }

    public void setDht(List<DHT> dht) {
        this.dht = dht;
    }

    public DRI getDri() {
        return dri;
    }

    public void setDri(DRI dri) {
        this.dri = dri;
    }

    public SOS getSos() {
        return sos;
    }

    public void setSos(SOS sos) {
        this.sos = sos;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    public Tag getEoi() {
        return eoi;
    }

    public void setEoi(Tag eoi) {
        this.eoi = eoi;
    }

    public List<ColorComponent> getColorComponent() {
        return colorComponent;
    }

    public void setColorComponent(List<ColorComponent> colorComponent) {
        this.colorComponent = colorComponent;
    }

    public List<Tag> getAllTags() {
        return allTags;
    }

    public void setAllTags(List<Tag> allTags) {
        this.allTags = allTags;
    }



}