package me.cizezsy;

import me.cizezsy.exception.JPEGDecoderException;
import me.cizezsy.huffman.HuffmanTable;
import me.cizezsy.jpeg.ColorComponent;
import me.cizezsy.jpeg.JPEGImage;
import me.cizezsy.jpeg.JPEGImage.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.Arrays;

public class JPEGImageDecoder {


    public JPEGImage decode(byte[] content) throws JPEGDecoderException {
        JPEGImage jpegImage = new JPEGImage();
        int currentPosition = 0;

        int[] unsignedByteContent = Arrays.stream(ArrayUtils.toObject(content))
                .mapToInt(value -> value & 0xff)
                .toArray();


        Tag soiTag = getTag(unsignedByteContent, currentPosition);
        if (soiTag.getTag() != JPEGImage.TAG_SOI)
            throw new JPEGDecoderException("Not a legal SOI");

        jpegImage.setSoi(soiTag);
        currentPosition += soiTag.getLength();

        Tag app0Tag = getTag(unsignedByteContent, currentPosition);
        if (app0Tag.getTag() != JPEGImage.TAG_APP0)
            throw new JPEGDecoderException("Not a legal APP0");

        jpegImage.setApp0(parseAPP0(app0Tag, unsignedByteContent));
        currentPosition += app0Tag.getLength();

        while (currentPosition < unsignedByteContent.length) {
            Tag tag = getTag(unsignedByteContent, currentPosition);
            switch (tag.getTag()) {
                case JPEGImage.TAG_DQT:
                    jpegImage.getDqt().add(parseDQT(tag, unsignedByteContent));
                    break;
                case JPEGImage.TAG_S0F0:
                    jpegImage.setSof0(parseSOF0(tag, unsignedByteContent, jpegImage));
                    break;
                case JPEGImage.TAG_DHT:
                    jpegImage.getDht().add(parseDHT(tag, unsignedByteContent));
                    break;
                case JPEGImage.TAG_DRI:
                    jpegImage.setDri(parseDRI(tag, unsignedByteContent));
                    break;
                case JPEGImage.TAG_SOS:
                    jpegImage.setSos(parseSOS(tag, unsignedByteContent, jpegImage));
                    break;
                case JPEGImage.TAG_IMAGE_DATA:
                    jpegImage.setImageData(parseImageData(tag, unsignedByteContent));
                    break;
                case JPEGImage.TAG_EOI:
                    jpegImage.setEoi(tag);
                    break;
                default:
                    if (tag.getTag() <= JPEGImage.TAG_APPN_MAX && tag.getTag() >= JPEGImage.TAG_APPN_MIN)
                        jpegImage.getAppn().add(tag);

                    if (tag.getTag() > JPEGImage.TAG_MAX || tag.getTag() < JPEGImage.TAG_MIN)
                        throw new JPEGDecoderException(String.format("Decode Error Not a legal tag 0x%x", tag.getTag()));

            }
            jpegImage.getAllTags().add(tag);
            currentPosition += tag.getLength();
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


    private JPEGImage.Tag getTag(int[] content, int position) {
        Tag result = new Tag();

        if (content[position] != 0xff) {
            result.setTag(JPEGImage.TAG_IMAGE_DATA);
            result.setPosition(position);
            result.setStuffing(0);
            result.setLength(content.length - position - 2);
            return result;
        }

        // when tag have many 0xff before, ignore them
        int start = position;
        while (content[position + 1] == 0xff)
            position++;

        result.setTag((content[position] << 8) + content[position + 1]);
        result.setPosition(start);
        result.setStuffing(position - start);

        if (result.getTag() == JPEGImage.TAG_SOI || result.getTag() == JPEGImage.TAG_EOI) {
            result.setLength(2 + result.getStuffing());
        } else {
            result.setLength((content[position + 2] << 8) + content[position + 3] + 2 + result.getStuffing());
        }
        return result;
    }

    private static JPEGImage.APP0 parseAPP0(JPEGImage.Tag tag, int[] data) {
        JPEGImage.APP0 app0 = new JPEGImage.APP0(tag);
        int start = tag.getPosition() + tag.getStuffing();
        app0.setDensityUnit(data[start + 11]);
        app0.setxDensity((data[start + 12] << 8) + data[start + 13]);
        app0.setyDensity((data[start + 14] << 8) + data[start + 15]);
        return app0;
    }


    private static JPEGImage.DQT parseDQT(JPEGImage.Tag tag, int[] data) {
        JPEGImage.DQT dqt = new JPEGImage.DQT(tag);
        int start = tag.getPosition() + tag.getStuffing();
        dqt.setId(data[start + 4] & 0b1111);
        dqt.setPrecision((data[start + 4] >> 4) & 0b1111);
        assert dqt.getPrecision() == 1 || dqt.getPrecision() == 0;
        dqt.setItemLength(64 * (dqt.getPrecision() + 1));
        return dqt;
    }


    private static SOF0 parseSOF0(Tag tag, int[] data, JPEGImage jpegImage) {
        SOF0 sof0 = new SOF0(tag);
        int start = tag.getPosition() + tag.getStuffing();
        sof0.setPrecision(data[start + 4]);
        sof0.setHeight((data[start + 5] << 8) + data[start + 6]);
        sof0.setWidth((data[start + 7] << 8) + data[start + 8]);
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


    private static DHT parseDHT(Tag tag, int[] data) {
        DHT dht = new DHT(tag);
        int start = tag.getPosition() + tag.getStuffing();
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
            dht.getHuffmanTables().add(new HuffmanTable(id, type, eachNodeNum, weights));
        }
        return dht;
    }

    private static DRI parseDRI(Tag tag, int[] data) {
        DRI dri = new DRI(tag);
        int start = tag.getPosition() + tag.getStuffing();
        dri.setInterval((data[start + 4] << 8) + data[start]);
        return dri;
    }

    private static SOS parseSOS(Tag tag, int[] data, JPEGImage jpegImage) {
        SOS sos = new SOS(tag);
        int start = tag.getPosition() + tag.getStuffing();
        int sampleNum = data[start + 4];

        for (int i = 0; i < sampleNum * 2; i += 2) {
            int colorId = data[start + 5 + i];
            ColorComponent component = jpegImage.getOrCreateColorComponent(colorId);
            component.setDcId((data[start + 6 + i] >> 4) & 0b1111);
            component.setAcId(data[start + 6 + i] & 0b1111);
        }

        return sos;
    }

    private static ImageData parseImageData(Tag tag, int[] data) {
        ImageData imageData = new ImageData(tag);
        imageData.setData(Arrays.copyOfRange(data, tag.getPosition(), tag.getPosition() + tag.getLength()));
        return imageData;
    }


}
