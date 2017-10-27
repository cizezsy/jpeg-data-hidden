package me.cizezsy;

import me.cizezsy.jpeg.JPEGImage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class JPEGImageWriter {

    public void write(JPEGImage jpegImage, String path) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        jpegImage.getAllTags().forEach(tag -> {
            try {
                bos.write(tag.write(jpegImage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(bos.toByteArray());
    }
}
