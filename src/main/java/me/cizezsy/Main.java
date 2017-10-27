package me.cizezsy;

import me.cizezsy.exception.JPEGDecoderException;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.jpeg.JPEGDataProcessor;
import me.cizezsy.jpeg.JPEGImage;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            JPEGImage jpegImage = new JPEGImageDecoder().decode("C:\\Users\\Administrator\\Desktop\\u=3323316342,2515962810&fm=27&gp=0.jpg");
            new JPEGDataProcessor(jpegImage).hide("143564536453634562");
//            new JPEGDataProcessor(jpegImage).testAlg();
//            JPEGImage jpegImage = new JPEGImageDecoder().decode("/home/zsy/test_out.jpg");
//            System.out.println(new JPEGDataProcessor(jpegImage).reveal());
        } catch (IOException | JPEGDecoderException | JPEGParseException e) {
            e.printStackTrace();
        }
    }
}
