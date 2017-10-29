package me.cizezsy;

import me.cizezsy.data.LSBDataProducer;
import me.cizezsy.data.LSBDataReceiver;
import me.cizezsy.exception.BitIOException;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.jpeg.JPEGImage;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        try {
            String data = "123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789";
            LSBDataProducer lsbDataProducer = new LSBDataProducer(data.getBytes());
            JPEGImage image = new JPEGImageReader(new FileInputStream("C:\\Users\\Administrator\\Desktop\\aaa.jpg"), lsbDataProducer).readImage();

            try {
                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test_out.jpg");
                fos.write(image.getData());;
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
            LSBDataReceiver lsbDataReceiver = new LSBDataReceiver();
            new JPEGImageReader(new FileInputStream("C:\\Users\\Administrator\\Desktop\\test_out.jpg"), lsbDataReceiver).readImage();
            System.out.println();
            System.out.println(new String(lsbDataReceiver.recoverData(), "UTF-8"));
        } catch (BitIOException | FileNotFoundException | JPEGParseException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
