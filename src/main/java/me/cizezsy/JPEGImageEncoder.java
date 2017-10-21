package me.cizezsy;

import me.cizezsy.exception.JPEGDecoderException;
import me.cizezsy.exception.JPEGParseException;
import sun.awt.image.codec.JPEGImageEncoderImpl;

import java.io.IOException;

public class JPEGImageEncoder {

    private JPEGImage jpegImage;

    public JPEGImageEncoder() {
    }

    public JPEGImageEncoder load(String path) {
        try {
            jpegImage = JPEGImage.parseJPEGImage(path);
        } catch (IOException | JPEGParseException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void encode(String content) throws JPEGDecoderException {
        if (jpegImage == null)
            throw new JPEGDecoderException("Jpeg file Not Load!!!");
    }

}
