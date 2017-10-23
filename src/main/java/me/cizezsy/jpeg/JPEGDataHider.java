package me.cizezsy.jpeg;

public class JPEGDataHider {

    public void hide(JPEGImage jpegImage, String data) {
        ColorComponent yColorComponent = jpegImage.getColorComponent().get(0);
        int h0 = yColorComponent.getHorizontalSample();
        int v0 = yColorComponent.getVerticalSample();
        int maxMcuX = (jpegImage.getSof0().getWidth() + 8 * h0 - 1) / (8 * h0);
        int maxMcuY = (jpegImage.getSof0().getHeight() + 8 * v0 - 1) / (8 * v0);

        
    }
}
