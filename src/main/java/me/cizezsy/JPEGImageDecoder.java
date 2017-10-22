package me.cizezsy;

import me.cizezsy.exception.JPEGDecoderException;
import me.cizezsy.huffman.HuffmanTable;
import me.cizezsy.jpeg.ColorComponent;
import me.cizezsy.jpeg.JPEGImage;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JPEGImageDecoder {

    public JPEGImage decode(byte[] content) throws JPEGDecoderException {
        JPEGImage jpegImage = new JPEGImage();
        int currentPosition = 0;

        int[] unsignedByteContent = Arrays.stream(ArrayUtils.toObject(content))
                .mapToInt(value -> value & 0xff)
                .toArray();


        Tag soiTag = getTag(unsignedByteContent, currentPosition);
        if (soiTag.tag != TAG_SOI)
            throw new JPEGDecoderException("Not a legal SOI");

        jpegImage.setSoi(soiTag);
        currentPosition += soiTag.length;

        Tag app0Tag = getTag(unsignedByteContent, currentPosition);
        if (app0Tag.tag != TAG_APP0)
            throw new JPEGDecoderException("Not a legal APP0");

        jpegImage.setApp0(APP0.parseAPP0(app0Tag, unsignedByteContent));
        currentPosition += app0Tag.length;

        while (currentPosition < unsignedByteContent.length) {
            Tag tag = getTag(unsignedByteContent, currentPosition);
            switch (tag.tag) {
                case TAG_DQT:
                    jpegImage.getDqt().add(DQT.parseDQT(tag, unsignedByteContent));
                    break;
                case TAG_S0F0:
                    jpegImage.setSof0(SOF0.parseSOF0(tag, unsignedByteContent, jpegImage));
                    break;
                case TAG_DHT:
                    jpegImage.getDht().add(DHT.parseDHT(tag, unsignedByteContent));
                    break;
                case TAG_DRI:
                    jpegImage.setDri(DRI.parseDRI(tag, unsignedByteContent));
                    break;
                case TAG_SOS:
                    jpegImage.setSos(SOS.parseSOS(tag, unsignedByteContent, jpegImage));
                    break;
                case TAG_IMAGE_DATA:
                    jpegImage.setImageData(ImageData.parseImageData(tag, unsignedByteContent));
                    break;
                case TAG_EOI:
                    jpegImage.setEoi(tag);
                    break;
                default:
                    if (tag.tag <= TAG_APPN_MAX && tag.tag >= TAG_APPN_MIN)
                        jpegImage.getAppn().add(tag);

                    if (tag.tag > TAG_MAX || tag.tag < TAG_MIN)
                        throw new JPEGDecoderException(String.format("Decode Error Not a legal tag 0x%x", tag.tag));

            }
            jpegImage.getAllTags().add(tag);
            currentPosition += tag.length;
        }
        jpegImage.setUnsignedByteData(unsignedByteContent);
        return jpegImage;
    }


    public JPEGImage decode(File file) throws IOException, JPEGDecoderException {
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

            return decode(result);
        }
    }

    public JPEGImage decode(String path) throws IOException, JPEGDecoderException {
        return decode(new File(path));
    }


    public Tag getTag(int[] content, int position) {
        Tag result = new Tag();

        if (content[position] != 0xff) {
            result.tag = TAG_IMAGE_DATA;
            result.position = position;
            result.stuffing = 0;
            result.length = content.length - position - 2;
            return result;
        }

        // when tag have many 0xff before, ignore them
        int start = position;
        while (content[position + 1] == 0xff)
            position++;

        result.tag = (content[position] << 8) + content[position + 1];
        result.position = start;
        result.stuffing = position - start;

        if (result.tag == TAG_SOI || result.tag == TAG_EOI) {
            result.length = 2 + result.stuffing;
        } else {
            result.length = (content[position + 2] << 8) + content[position + 3] + 2 + result.stuffing;
        }
        return result;
    }

    public static class Tag {
        protected int tag;
        protected int position;
        protected int length;
        protected int stuffing;
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
    public static class APP0 extends Tag {
        private int densityUnit;
        private int xDensity;
        private int yDensity;

        private static APP0 parseAPP0(Tag tag, int[] data) {
            APP0 app0 = new APP0(tag);
            int start = tag.position + tag.stuffing;
            app0.densityUnit = data[start + 11];
            app0.xDensity = (data[start + 12] << 8) + data[start + 13];
            app0.yDensity = (data[start + 14] << 8) + data[start + 15];
            return app0;
        }

        private APP0(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }
    }

    /**
     * DQT                                                          Define Quantization Table，定义量化表
     * 标记代码                           2                         固定值0xFFDB
     * 包含9个具体字段：
     * ① 数据长度                        2                         字段①和多个字段②的总长度 即不包括标记代码，但包括本字段
     * ② 量化表
     * a)精度及量化表ID                   1                         高4位：精度，只有两个可选值 0：8位；1：16位 低4位：量化表ID，取值范围为0～3
     * b)表项                        (64×(精度+1))                 例如8位精度的量化表 其表项长度为64×（0+1）=64字节
     */
    public static class DQT extends Tag {
        private int id;
        private int precision;
        private int itemLength;

        private static DQT parseDQT(Tag tag, int[] data) {
            DQT dqt = new DQT(tag);
            int start = tag.position + tag.stuffing;
            dqt.id = data[start + 4] & 0b1111;
            dqt.precision = (data[start + 4] >> 4) & 0b1111;
            assert dqt.precision == 1 || dqt.precision == 0;
            dqt.itemLength = 64 * (dqt.precision + 1);
            return dqt;
        }

        private DQT(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

    }

    /**
     * SOF0                                               Start of Frame，帧图像开始
     * u 标记代码                   2     固定值0xFFC0
     * u 包含9个具体字段：
     * ① 数据长度                  2     ①~⑥六个字段的总长度 即不包括标记代码，但包括本字段
     * ② 精度                      1     每个数据样本的位数 通常是8位，一般软件都不支持 12位和16位
     * ③ 图像高度                  2     图像高度（单位：像素），如果不支持 DNL 就必须 >0
     * ④ 图像宽度                  2     图像宽度（单位：像素），如果不支持 DNL 就必须 >0
     * ⑤ 颜色分量数                1     只有3个数值可选 1：灰度图；3：YCrCb或YIQ；4：CMYK  而JFIF中使用YCrCb，故这里颜色分量数恒为3
     * ⑥颜色分量信息                     颜色分量数×3（通常为9）
     * a)颜色分量ID                 1
     * b)水平/垂直采样因子          1     高4位：水平采样因子 低4位：垂直采样因子
     * c) 量化表                    1     当前分量使用的量化表的ID
     */
    public static class SOF0 extends Tag {
        private int precision;
        int width;
        int height;

        private static SOF0 parseSOF0(Tag tag, int[] data, JPEGImage jpegImage) {
            SOF0 sof0 = new SOF0(tag);
            int start = tag.position + tag.stuffing;
            sof0.precision = data[start + 4];
            sof0.height = (data[start + 5] << 8) + data[start + 6];
            sof0.width = (data[start + 7] << 8) + data[start + 8];
            int sampleNum = data[start + 9];
            for (int i = 0; i < sampleNum * 3; i += 3) {
                int colorId = data[start + 10 + i];
                ColorComponent component = jpegImage.getOrCreateColorComponent(colorId);
                component.setHorizontalSample((data[start + 11 + i] >> 4) & 0b1111);
                component.setVerticalSample(data[start + 11 + i] & 0b1111);
                component.setQtId(data[start + 12 + i]);
            }
            return sof0;
        }

        private SOF0(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }

        public int getPrecision() {
            return precision;
        }

        public void setPrecision(int precision) {
            this.precision = precision;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public static class DHT extends Tag {
        private List<HuffmanTable> huffmanTables = new ArrayList<>();

        private static DHT parseDHT(Tag tag, int[] data) {
            DHT dht = new DHT(tag);
            int start = tag.position + tag.stuffing;
            for (int sumHuffmanTableLength = (data[start + 2] << 8) + data[start + 3] - 2, i = 0; sumHuffmanTableLength > 0; ) {

                int type = data[start + 4 + i] >> 4 & 0b1111;
                int id = data[start + 4 + i] & 0b1111;
                sumHuffmanTableLength -= 1;

                int[] eachNodeNum = Arrays.copyOfRange(data, start + i + 5, start + i + 21);
                sumHuffmanTableLength -= 16;

                int tableLength = 0;
                for (int b : eachNodeNum) {
                    tableLength += b;
                }
                i += tableLength;
                sumHuffmanTableLength -= tableLength;


                int[] weights = Arrays.copyOfRange(data, start + i + 21, start + i + tableLength + 21);
                dht.huffmanTables.add(new HuffmanTable(id, type, eachNodeNum, weights));
            }
            return dht;
        }

        private DHT(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }
    }

    public static class DRI extends Tag {
        private int interval;

        private static DRI parseDRI(Tag tag, int[] data) {
            DRI dri = new DRI(tag);
            int start = tag.position + tag.stuffing;
            dri.interval = (data[start + 4] << 8) + data[start];
            return dri;
        }

        private DRI(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }
    }

    public static class SOS extends Tag {

        private static SOS parseSOS(Tag tag, int[] data, JPEGImage jpegImage) {
            SOS sos = new SOS(tag);
            int start = tag.position + tag.stuffing;
            int sampleNum = data[start + 5];

            for (int i = 0; i < sampleNum * 2; i += 2) {
                int colorId = data[start + 6 + i];
                ColorComponent component = jpegImage.getOrCreateColorComponent(colorId);
                component.setDcId((data[start + 7 + i] >> 4) & 0b1111);
                component.setAcId(data[start + 7 + i] & 0b1111);
            }

            return sos;
        }

        private SOS(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }
    }

    public static class ImageData extends Tag {
        private int[] data;

        private static ImageData parseImageData(Tag tag, int[] data) {
            ImageData imageData = new ImageData(tag);
            imageData.data = Arrays.copyOfRange(data, tag.position, tag.position + tag.length);
            return imageData;
        }

        private ImageData(Tag tag) {
            this.tag = tag.tag;
            this.position = tag.position;
            this.stuffing = tag.stuffing;
            this.length = tag.length;
        }
    }


    private static final int TAG_SOI = 0xffd8;
    private static final int TAG_APP0 = 0xffe0;
    private static final int TAG_APPN_MIN = 0xffe1;
    private static final int TAG_APPN_MAX = 0xffef;
    private static final int TAG_DQT = 0xffdb;
    private static final int TAG_S0F0 = 0xffc0;
    private static final int TAG_DHT = 0xffc4;
    private static final int TAG_DRI = 0xffdd;
    private static final int TAG_SOS = 0xffda;
    private static final int TAG_EOI = 0xffd9;

    private static final int TAG_IMAGE_DATA = 0;
    private static final int TAG_MIN = 0xff01;
    private static final int TAG_MAX = 0xfffe;


    public static void main(String[] args) {
        try {
            new JPEGImageDecoder().decode("C:\\Users\\Administrator\\Desktop\\u=3323316342,2515962810&fm=27&gp=0.jpg");
        } catch (IOException | JPEGDecoderException e) {
            e.printStackTrace();
        }
    }
}
