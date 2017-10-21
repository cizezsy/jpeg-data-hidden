package me.cizezsy;

import me.cizezsy.exception.JPEGParseException;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JPEGImage {
    private byte[] soi;
    private APP0 app0;
    private APPN[] appn;
    private DQT[] dqt;
    private SOF0 sof0;
    private DHT[] dht;
    private DRI dri;
    private SOS sos;
    private ImageData imageData;
    private byte[] eoi;

    private JPEGImage() {
    }

    public static JPEGImage parseJPEGImage(byte[] content) throws JPEGParseException {
        JPEGImage jpegImage = new JPEGImage();

        int currentPosition = 0;
        if (content[currentPosition++] != 0xff && content[currentPosition++] != 0xd8)
            throw new JPEGParseException("Not a legal SOI");

        jpegImage.soi = new byte[]{(byte) 0xff, (byte) 0xd8};

        if (content[currentPosition] != 0xff && content[currentPosition + 1] != 0xe0) {
            throw new JPEGParseException("Not a legal APP0");
        }

        int app0Length = (content[currentPosition + 2] << 8) + content[currentPosition + 3] + 2;
        jpegImage.app0 = APP0.parseAPP0(Arrays.copyOfRange(content, currentPosition, currentPosition + app0Length));
        currentPosition += app0Length;

        List<APPN> appnList = new LinkedList<>();
        while (content[currentPosition] == 0xff
                && content[currentPosition + 1] >= 0xe1 && content[currentPosition + 1] <= 0xef) {
            int appnLength = (content[currentPosition + 2] << 8) + content[currentPosition + 3] + 2;
            APPN appn = APPN.parseAPPN(Arrays.copyOfRange(content, currentPosition, currentPosition + appnLength));
            appnList.add(appn);
            currentPosition += appnLength;
        }
        jpegImage.appn = appnList.toArray(new APPN[appnList.size()]);

        if (content[currentPosition] != 0xff && content[currentPosition + 1] != 0xdb)
            throw new JPEGParseException("Not a legal DQT");

        List<DQT> dqtList = new LinkedList<>();
        while (content[currentPosition] == 0xff && content[currentPosition + 1] == 0xdb) {
            int dqtLength = content[currentPosition + 2] << 8 + content[currentPosition + 3] + 2;
            DQT dqt = DQT.parseDQT(Arrays.copyOfRange(content, currentPosition, currentPosition + dqtLength));
            dqtList.add(dqt);
            currentPosition += dqtLength;
        }
        jpegImage.dqt = dqtList.toArray(new DQT[dqtList.size()]);

        if (content[currentPosition] != 0xff && content[currentPosition + 1] != 0xc0)
            throw new JPEGParseException("Not a legal SOF0");

        int sof0length = content[currentPosition + 2] << 8 + content[currentPosition + 3] + 2;
        jpegImage.sof0 = SOF0.parseSOF0(Arrays.copyOfRange(content, currentPosition, currentPosition + sof0length));
        currentPosition += sof0length;

        if (content[currentPosition] != 0xff && content[currentPosition + 1] != 0xc4)
            throw new JPEGParseException("Not a legal DHT");

        List<DHT> dhtList = new LinkedList<>();
        while (content[currentPosition] == 0xff && content[currentPosition + 1] == 0xc4) {
            int dhtLength = content[currentPosition + 2] << 8 + content[currentPosition + 3] + 2;
            DHT dht = DHT.parseDHT(Arrays.copyOfRange(content, currentPosition, currentPosition + dhtLength));
            dhtList.add(dht);
            currentPosition += dhtLength;
        }
        jpegImage.dht = dhtList.toArray(new DHT[dhtList.size()]);

        if (content[currentPosition] == 0xff && content[currentPosition] == 0xdd) {
            int driLength = content[currentPosition + 2] << 8 + content[currentPosition + 3] + 2;
            jpegImage.dri = DRI.parseDRI(Arrays.copyOfRange(content, currentPosition, currentPosition + driLength));
            currentPosition += driLength;
        }

        if (content[currentPosition] != 0xff && content[currentPosition] != 0xda)
            throw new JPEGParseException("Not a legal SOS");

        int sosLength = content[currentPosition + 2] << 8 + content[currentPosition + 3] + 2;
        jpegImage.sos = SOS.parseSOS(Arrays.copyOfRange(content, currentPosition, currentPosition + sosLength));
        currentPosition += sosLength;

        if (content[content.length - 2] != 0xff && content[content.length - 1] != 0xd9)
            throw new JPEGParseException("Not a legal EOI");

        jpegImage.eoi = new byte[]{(byte) 0xff, (byte) 0xd9};

        jpegImage.imageData = ImageData.parseImageData(Arrays.copyOfRange(content, currentPosition, content.length - 2));

        return jpegImage;
    }

    public static JPEGImage parseJPEGImage(File file) throws IOException, JPEGParseException {
        if (!file.exists())
            throw new FileNotFoundException("File not Exist");

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            byte[] result = baos.toByteArray();

            if (result == null || result.length == 0)
                throw new IOException("Read File Error: There is no content");

            return parseJPEGImage(result);
        }
    }

    public static JPEGImage parseJPEGImage(String path) throws IOException, JPEGParseException {
        return parseJPEGImage(new File(path));
    }

    /**
     * APP0                              length  Application，应用程序保留标记0
     * 标记代码                            2     固定值0xFFE0
     * 包含9个具体字段：
     * ① 数据长度                         2     ①~⑨9个字段的总长 即不包括标记代码，但包括本字段
     * ② 标识符                           5     固定值0x4A46494600，即字符串“JFIF0”
     * ③ 版本号                           2     一般是0x0102，表示JFIF的版本号1.2 可能会有其他数值代表其他版本
     * ④ X和Y的密度单位                   1     只有三个值可 0：无单位；1：点数/英寸；2：点数/厘米
     * ⑤ X方向像素密度                    2     取值范围未知
     * ⑥ Y方向像素密度                    2     取值范围未知
     * ⑦ 缩略图水平像素数目               1     取值范围未知
     * ⑧ 缩略图垂直像素数目               1     取值范围未知
     * ⑨ 缩略图RGB位图                    3x    缩略图RGB位图数据
     */
    private static class APP0 {
        private byte[] rawData;
        private int dataLength;
        private int densityUnit;
        private int xDensity;
        private int yDensity;

        private static APP0 parseAPP0(byte[] data) {
            APP0 app0 = new APP0();
            app0.rawData = data;
            app0.dataLength = data[2] << 8 + data[3];
            app0.densityUnit = data[11];
            app0.xDensity = data[12] << 8 + data[13];
            app0.yDensity = data[14] << 8 + data[15];
            return app0;
        }
    }

    private static class APPN {

        private byte[] rawData;

        private static APPN parseAPPN(byte[] data) {
            APPN appn = new APPN();
            appn.rawData = data;
            return appn;

        }
    }

    private static class DQT {
        private byte[] rawData;
        private int dataLength;

        private static DQT parseDQT(byte[] data) {
            return new DQT();
        }

        
    }

    private static class SOF0 {
        private static SOF0 parseSOF0(byte[] data) {
            return new SOF0();
        }
    }

    private static class DHT {
        private static DHT parseDHT(byte[] data) {
            return new DHT();
        }
    }

    private static class DRI {
        private static DRI parseDRI(byte[] data) {
            return new DRI();
        }
    }

    private static class SOS {
        private static SOS parseSOS(byte[] data) {
            return new SOS();
        }
    }

    private static class ImageData {
        private static ImageData parseImageData(byte[] data) {
            return new ImageData();
        }
    }
}
