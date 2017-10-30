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
            String data = "想要这样的安卓机 \n" +
                    "1 颜色显示准，不要很艳，三星的 oled 虽然很漂亮，但对我太艳了 \n" +
                    "2 运行内存至少 4G \n" +
                    "3 续航说的过去，中度使用大半天到一天 \n" +
                    "4 价格越便宜越好 \n" +
                    "5 尽量不要是国产小米华为之类，而且说实话小米华为的屏幕好像色准一般 ";
            LSBDataProducer lsbDataProducer = new LSBDataProducer(data.getBytes());
            JPEGImage image = new JPEGImageReader(new FileInputStream("C:\\Users\\Administrator\\Desktop\\aaa.jpg"), lsbDataProducer).readImage();

            try {
                FileOutputStream fos = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test_out.jpg");
                fos.write(image.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
            LSBDataReceiver lsbDataReceiver = new LSBDataReceiver();
            new JPEGImageReader(new FileInputStream("C:\\Users\\Administrator\\Desktop\\test_out.jpg"), lsbDataReceiver).readImage();
            System.out.println();
            System.out.println(new String(lsbDataReceiver.recoverData()));
        } catch (BitIOException | FileNotFoundException | JPEGParseException e) {
            e.printStackTrace();
        }
    }

}
