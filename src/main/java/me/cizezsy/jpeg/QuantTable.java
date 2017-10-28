package me.cizezsy.jpeg;

public class QuantTable {
    private int precision;
    private int id;
    private int[][] quant;

    public QuantTable(int precision, int id, int[][] quant) {
        this.precision = precision;
        this.id = id;
        this.quant = quant;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[][] getQuant() {
        return quant;
    }

    public void setQuant(int[][] quant) {
        this.quant = quant;
    }
}
