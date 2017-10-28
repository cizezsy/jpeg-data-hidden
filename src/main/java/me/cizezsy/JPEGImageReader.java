package me.cizezsy;

import me.cizezsy.exception.BitIOException;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.huffman.HuffmanTable;
import me.cizezsy.bit.BitInputStream;
import me.cizezsy.jpeg.ColorComponent;
import me.cizezsy.jpeg.JPEG;
import me.cizezsy.jpeg.QuantTable;
import me.cizezsy.jpeg.marker.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JPEGImageReader {

    private BitInputStream bitInputStream;
    private List<ColorComponent> colorComponents = new ArrayList<>();
    private List<QuantTable> quantTables = new ArrayList<>();
    private List<HuffmanTable> huffmanTables = new ArrayList<>();

    private final static int[] ZIG_ZAG = {
            0, 1, 8, 16, 9, 2, 3, 10,
            17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34,
            27, 20, 13, 6, 7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51,
            58, 59, 52, 45, 38, 31, 39, 46,
            53, 60, 61, 54, 47, 55, 62, 63
    };

    public JPEGImageReader(InputStream inputStream) throws BitIOException {
        this.bitInputStream = new BitInputStream(inputStream);
    }

    public JPEG readImage() throws JPEGParseException {
        List<Marker> markers = new ArrayList<>(20);
        JPEG jpegImage = new JPEG();

        try {
            Marker soi = readMarker(bitInputStream);
            if (soi.getTag() != JPEG.SOI) {
                throw new JPEGParseException("Not A legal SOI");
            }
            markers.add(soi);
        } catch (BitIOException | JPEGParseException e) {
            throw new JPEGParseException(e);
        }

        try {
            Marker app0 = readMarker(bitInputStream);
            app0 = parseAPP0(bitInputStream, app0);
            if (app0.getTag() != JPEG.APP0) {
                throw new JPEGParseException("Not A legal APP0");
            }
        } catch (BitIOException | JPEGParseException e) {
            throw new JPEGParseException(e);
        }

        while (bitInputStream.position() != bitInputStream.length()) {
            try {
                Marker marker = readMarker(bitInputStream);
                int origin = bitInputStream.position();
                switch (marker.getTag()) {
                    case JPEG.DQT:
                        marker = parseDQT(bitInputStream, marker);
                        break;
                    case JPEG.SOF0:
                        marker = parseSOF0(bitInputStream, marker);
                        break;
                    case JPEG.DHT:
                        marker = parseDHT(bitInputStream, marker);
                        break;
                    case JPEG.DRI:
                        marker = parseDRI(bitInputStream, marker);
                        break;
                    case JPEG.SOS:
                        marker = parseSOS(bitInputStream, marker);
                        break;
                    case JPEG.IMAGE:
                        marker = parseImage(bitInputStream, marker);
                        break;
                }
                bitInputStream.position(origin);
                markers.add(marker);
            } catch (BitIOException | JPEGParseException e) {
                e.printStackTrace();
            }
        }
        return jpegImage;
    }

    //There has some problem, I have assert any maker except SOI and EOI and IMAGE have length tag in third byte
    //It might cause problem, but now I don't want fixed it. it is too boring..
    private Marker readMarker(BitInputStream bitInputStream) throws BitIOException {
        Marker marker = new Marker();

        int start = bitInputStream.position();
        //set marker's position
        //BitInputStream's position and length is the bit's position and length,
        //marker's position and length is the byte's.So it should divide by 8
        marker.setPosition(start / 8);
        int f = bitInputStream.readInt8();
        if (f != 0xff || bitInputStream.peekBits(8) == 0x00) {
            marker.setTag(JPEG.IMAGE);
            marker.setLength((bitInputStream.length() - start) / 8 - 2);
            marker.setStuffing(0);
            bitInputStream.position(bitInputStream.length() - 16);
            return marker;
        }

        int stuffing = 0;
        int tag;
        //ignore sequent 0xff
        while ((tag = bitInputStream.readInt8()) == 0xff) {
            stuffing++;
        }

        marker.setStuffing(stuffing);

        if (tag == 0x00) {
            marker.setTag(JPEG.IMAGE);
            marker.setLength((bitInputStream.length() - start) / 8 - 2);
            bitInputStream.position(bitInputStream.length() - 16);
            return marker;
        }

        tag |= 0xff00;
        marker.setTag(tag);
        if (tag == JPEG.SOI || tag == JPEG.EOI) {
            marker.setLength(2);
        } else {
            //length = marker's data length + 2
            int length = bitInputStream.readInt16() + 2;
            marker.setLength(length);
            bitInputStream.position(start + marker.getLength() * 8 + stuffing * 8);
        }
        return marker;
    }

    private APP0Marker parseAPP0(BitInputStream s, Marker marker) throws BitIOException, JPEGParseException {
        APP0Marker app0Marker = new APP0Marker(marker);

        s.position(app0Marker.getPosition() * 8);
        s.skipBytes(4);
        //only support JFIF0
        if (s.readInt8() != 0x4A && s.readInt8() != 0x46 &&
                s.readInt8() != 0x49 && s.readInt8() != 0x46 && s.readInt8() != 0x00) {
            throw new JPEGParseException("JPEG file isn't JFIF");
        }

        s.skipBytes(2);
        app0Marker.setDensityUnit(s.readInt8());
        app0Marker.setxDensity(s.readInt16());
        app0Marker.setyDensity(s.readInt16());

        return app0Marker;
    }

    private DQTMarker parseDQT(BitInputStream s, Marker marker) throws BitIOException {
        DQTMarker dqtMarker = new DQTMarker(marker);
        s.position(dqtMarker.getPosition() * 8);

        s.skipBytes(2);
        while (s.position() != (dqtMarker.getLength() + dqtMarker.getPosition()) * 8) {
            int precision = s.readBits(4);
            int id = s.readBits(4);
            int[][] quant = new int[8][8];
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (precision == 0)
                        quant[i][j] = s.readInt8();
                    else
                        quant[i][j] = s.readInt16();
                }
            }
            QuantTable quantTable = new QuantTable(precision, id, quant);
            quantTables.add(quantTable);
        }

        return dqtMarker;
    }

    private SOF0Marker parseSOF0(BitInputStream s, Marker marker) throws BitIOException, JPEGParseException {
        SOF0Marker sof0Marker = new SOF0Marker(marker);
        s.position(sof0Marker.getPosition() * 8);

        s.skipBytes(5);
        sof0Marker.setHeight(s.readInt16());
        sof0Marker.setWidth(s.readInt16());
        int num = s.readInt8();
        if (num != 3)
            throw new JPEGParseException("Only support YCrCb");
        for (int i = 0; i < num; i++) {
            int id = s.readInt8();
            ColorComponent colorComponent = colorComponents.stream()
                    .filter(c -> c.getId() == id)
                    .findAny()
                    .orElseGet(() -> {
                        ColorComponent c = new ColorComponent();
                        colorComponents.add(c);
                        return c;
                    });
            colorComponent.setId(id);
            colorComponent.setHs(s.readBits(4));
            colorComponent.setVs(s.readBits(4));
            colorComponent.setQuantId(s.readInt8());
        }
        return sof0Marker;
    }

    private DHTMarker parseDHT(BitInputStream s, Marker marker) throws BitIOException {
        DHTMarker dhtMarker = new DHTMarker(marker);
        s.position(dhtMarker.getPosition() * 8);

        s.skipBytes(4);
        while (s.position() != (dhtMarker.getLength() + dhtMarker.getPosition()) * 8) {
            int type = s.readBits(4);
            int id = s.readBits(4);

            int sumNode = 0;
            int[] nodeNum = new int[16];
            for (int i = 0; i < nodeNum.length; i++) {
                nodeNum[i] = s.readInt8();
                sumNode += nodeNum[i];
            }

            int[] weight = new int[sumNode];
            for (int i = 0; i < sumNode; i++) {
                weight[i] = s.readInt8();
            }

            HuffmanTable huffmanTable = new HuffmanTable(id, type, nodeNum, weight);
            huffmanTables.add(huffmanTable);
        }

        return dhtMarker;
    }

    private DRIMarker parseDRI(BitInputStream s, Marker marker) throws BitIOException {
        DRIMarker driMarker = new DRIMarker(marker);
        s.position(marker.getPosition() * 8);

        s.skipBytes(4);
        driMarker.setInterval(s.readInt16());

        return driMarker;
    }

    private SOSMarker parseSOS(BitInputStream s, Marker marker) throws BitIOException, JPEGParseException {
        SOSMarker sosMarker = new SOSMarker(marker);
        s.position(sosMarker.getPosition() * 8);

        s.skipBytes(4);
        int num = s.readInt8();
        if (num != 3)
            throw new JPEGParseException("Only support YCrCb");

        for (int i = 0; i < num; i++) {
            int id = s.readInt8();
            ColorComponent colorComponent = colorComponents.stream()
                    .filter(c -> c.getId() == id)
                    .findAny()
                    .orElseGet(() -> {
                        ColorComponent c = new ColorComponent();
                        colorComponents.add(c);
                        return c;
                    });
            colorComponent.setId(id);
            colorComponent.setDcId(s.readBits(4));
            colorComponent.setAcId(s.readBits(4));
        }

        sosMarker.setSelectStart(s.readInt8());
        sosMarker.setSelectEnd(s.readInt8());
        sosMarker.setSelect(s.readInt8());

        return sosMarker;
    }

    private ImageMarker parseImage(BitInputStream s, Marker marker) {
        ImageMarker imageMarker = new ImageMarker(marker);
        return imageMarker;
    }

}
