package me.cizezsy.jpeg;

public class ColorComponent {
    private int id;
    private int hs;
    private int vs;
    private int quantId;
    private int acId;
    private int dcId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHs() {
        return hs;
    }

    public void setHs(int hs) {
        this.hs = hs;
    }

    public int getVs() {
        return vs;
    }

    public void setVs(int vs) {
        this.vs = vs;
    }

    public int getQuantId() {
        return quantId;
    }

    public void setQuantId(int quantId) {
        this.quantId = quantId;
    }

    public int getAcId() {
        return acId;
    }

    public void setAcId(int acId) {
        this.acId = acId;
    }

    public int getDcId() {
        return dcId;
    }

    public void setDcId(int dcId) {
        this.dcId = dcId;
    }
}
