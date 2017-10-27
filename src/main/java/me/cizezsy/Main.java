package me.cizezsy;

import me.cizezsy.exception.JPEGDecoderException;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.jpeg.JPEGDataHider;
import me.cizezsy.jpeg.JPEGImage;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            JPEGImage jpegImage = new JPEGImageDecoder().decode("C:\\Users\\Administrator\\Desktop\\u=3323316342,2515962810&fm=27&gp=0.jpg");
            new JPEGDataHider(jpegImage).hide("asd");
//            new JPEGDataHider(jpegImage).testAlg();
        } catch (IOException | JPEGDecoderException | JPEGParseException e) {
            e.printStackTrace();
        }
    }
}
