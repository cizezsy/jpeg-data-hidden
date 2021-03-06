package me.cizezsy;

import me.cizezsy.bit.BitMap;
import me.cizezsy.bit.BitPool;
import me.cizezsy.bit.BitsBuffer;
import me.cizezsy.data.LSBDataAction;
import me.cizezsy.data.LSBDataProducer;
import me.cizezsy.data.LSBDataReceiver;
import me.cizezsy.exception.BitIOException;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.huffman.HuffmanTable;
import me.cizezsy.jpeg.ColorComponent;
import me.cizezsy.jpeg.JPEG;
import me.cizezsy.jpeg.JPEGImage;
import me.cizezsy.jpeg.QuantTable;
import me.cizezsy.jpeg.marker.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JPEGImageReader {

    private final LSBDataAction lsbDataAction;
    private final BitMap bitMap;
    private List<ColorComponent> colorComponents = new ArrayList<>();
    private List<QuantTable> quantTables = new ArrayList<>();
    private List<HuffmanTable> huffmanTables = new ArrayList<>();
    private JPEGImage jpegImage;

    private ColorComponent yC;
    private ColorComponent crC;
    private ColorComponent cbC;

    private HuffmanTable yAcHuffmanTable;
    private HuffmanTable yDcHuffmanTable;
    private HuffmanTable crCbAcHuffmanTable;
    private HuffmanTable crCbDcHuffmanTable;

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
    private final BitPool bitPool = new BitPool();

    public JPEGImageReader(InputStream inputStream, LSBDataAction lsbDataAction) throws BitIOException {
        this.bitMap = new BitMap(inputStream);
        this.lsbDataAction = lsbDataAction;
    }

    public JPEGImage readImage() throws JPEGParseException, BitIOException {
        List<Marker> markers = new ArrayList<>(20);
        jpegImage = new JPEGImage();

        try {
            Marker soi = readMarker(bitMap);
            if (soi.getTag() != JPEG.SOI) {
                throw new JPEGParseException("Not A legal SOI");
            }
            markers.add(soi);
            bitPool.writeInt16(soi.getTag(), true);
        } catch (BitIOException | JPEGParseException e) {
            throw new JPEGParseException(e);
        }

        try {
            Marker app0 = readMarker(bitMap);
            int origin = bitMap.position();
            app0 = parseAPP0(bitMap, app0);
            if (app0.getTag() != JPEG.APP0) {
                throw new JPEGParseException("Not A legal APP0");
            }
            jpegImage.setApp0Marker((APP0Marker) app0);
            bitMap.position(origin);
            writeMarker(bitPool, bitMap, app0);
        } catch (BitIOException | JPEGParseException e) {
            throw new JPEGParseException(e);
        }

        while (bitMap.position() != bitMap.length()) {
            Marker marker = readMarker(bitMap);
            int origin = bitMap.position();
            switch (marker.getTag()) {
                case JPEG.DQT:
                    marker = parseDQT(bitMap, marker);
                    jpegImage.setDqtMarker((DQTMarker) marker);
                    writeMarker(bitPool, bitMap, marker);
                    break;
                case JPEG.SOF0:
                    marker = parseSOF0(bitMap, marker);
                    jpegImage.setSof0Marker((SOF0Marker) marker);
                    writeMarker(bitPool, bitMap, marker);
                    break;
                case JPEG.DHT:
                    marker = parseDHT(bitMap, marker);
                    jpegImage.setDhtMarker((DHTMarker) marker);
                    writeMarker(bitPool, bitMap, marker);
                    break;
                case JPEG.DRI:
                    marker = parseDRI(bitMap, marker);
                    jpegImage.setDriMarker((DRIMarker) marker);
                    writeMarker(bitPool, bitMap, marker);
                    break;
                case JPEG.SOS:
                    marker = parseSOS(bitMap, marker);
                    jpegImage.setSosMarker((SOSMarker) marker);
                    writeMarker(bitPool, bitMap, marker);
                    break;
                case JPEG.IMAGE:
                    marker = parseImage(bitMap, marker);
                    jpegImage.setData(((ImageMarker) marker).getData());
                    break;
                default:
                    markers.add(marker);
                    if (marker.getTag() == JPEG.EOI) {
                        bitPool.writeInt16(JPEG.EOI, true);
                        break;
                    } else {
                        marker = parseImage(bitMap, marker);
                    }
            }
            bitMap.position(origin);
            markers.add(marker);
        }
        jpegImage.setData(bitPool.toByteArray());
        return jpegImage;
    }

    private void writeMarker(BitPool bitPool, BitMap bitMap, Marker marker) throws BitIOException {
        int position = marker.getPosition() * 8;
        int origin = bitMap.position();
        bitMap.position(position);

        int length = marker.getLength();
        for (int i = 0; i < length; i++) {
            bitPool.writeInt8(bitMap.readInt8(), true);
        }
        bitMap.position(origin);
    }

    //There has some problem, I have assert any maker except SOI and EOI and IMAGE have length tag in third byte
    //It might cause problem, but now I don't want fixed it. it is too boring..
    private Marker readMarker(BitMap bitMap) throws BitIOException {
        Marker marker = new Marker();

        int start = bitMap.position();
        //set marker's position
        //BitMap's position and length is the bit's position and length,
        //marker's position and length is the byte's.So it should divide by 8
        marker.setPosition(start / 8);
        int f = bitMap.readInt8();
        if (f != 0xff || bitMap.peekBits(8) == 0x00) {
            marker.setTag(JPEG.IMAGE);
            marker.setLength((bitMap.length() - start) / 8 - 2);
            marker.setStuffing(0);
            bitMap.position(bitMap.length() - 16);
            return marker;
        }

        int stuffing = 0;
        int tag;
        //ignore sequent 0xff
        while ((tag = bitMap.readInt8()) == 0xff) {
            stuffing++;
        }

        marker.setStuffing(stuffing);

        if (tag == 0x00) {
            marker.setTag(JPEG.IMAGE);
            marker.setLength((bitMap.length() - start) / 8 - 2);
            bitMap.position(bitMap.length() - 16);
            return marker;
        }

        tag |= 0xff00;
        marker.setTag(tag);
        if (tag == JPEG.SOI || tag == JPEG.EOI) {
            marker.setLength(2);
        } else {
            //length = marker's data length + 2
            int length = bitMap.readInt16() + 2;
            marker.setLength(length);
            bitMap.position(start + marker.getLength() * 8 + stuffing * 8);
        }
        return marker;
    }

    private APP0Marker parseAPP0(BitMap s, Marker marker) throws BitIOException, JPEGParseException {
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

    private DQTMarker parseDQT(BitMap s, Marker marker) throws BitIOException {
        DQTMarker dqtMarker = new DQTMarker(marker);
        s.position(dqtMarker.getPosition() * 8);

        s.skipBytes(4);
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

    private SOF0Marker parseSOF0(BitMap s, Marker marker) throws BitIOException, JPEGParseException {
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

    private DHTMarker parseDHT(BitMap s, Marker marker) throws BitIOException {
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

    private DRIMarker parseDRI(BitMap s, Marker marker) throws BitIOException {
        DRIMarker driMarker = new DRIMarker(marker);
        s.position(marker.getPosition() * 8);

        s.skipBytes(4);
        driMarker.setInterval(s.readInt16());

        return driMarker;
    }

    private SOSMarker parseSOS(BitMap s, Marker marker) throws BitIOException, JPEGParseException {
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

    private ImageMarker parseImage(BitMap s, Marker marker) throws BitIOException {

        ImageMarker imageMarker = new ImageMarker(marker);
        s.position(marker.getPosition() * 8);
        BitsBuffer bitsBuffer = new BitsBuffer(s);
        int width = jpegImage.getSof0Marker().getWidth();
        int height = jpegImage.getSof0Marker().getHeight();

        initProperties();

        int hsY = yC.getHs();
        int vsY = yC.getVs();

        int mcuX = (width + hsY * 8 - 1) / (hsY * 8);
        int mcuY = (height + vsY * 8 - 1) / (vsY * 8);

        int[] prevDc = {0, 0, 0};
        for (int x = 0; x < mcuX; x++) {
            for (int y = 0; y < mcuY; y++) {
                for (int unit = 0; unit < hsY * vsY + 2; unit++) {
                    int[][] block = readBlock(bitsBuffer, prevDc, unit, lsbDataAction, bitPool);
                    //when i writeBit decoder, i'll process block in that file
                    //processBlock(block);
                }
            }
        }
        bitPool.writeBits(bitsBuffer.peekBits(bitsBuffer.getBufferLength()), bitsBuffer.getBufferLength());
        imageMarker.setData(bitsBuffer.getBitMap().getBits());
        return imageMarker;
    }

    private int[][] readBlock(BitsBuffer bitsBuffer, int[] prevDc, int unit, LSBDataAction lsbDataAction, BitPool bitPool) throws BitIOException {
        QuantTable quantTable = selectQuantTable(unit);

        int[][] block = new int[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int codeLength = 1;
                HuffmanTable huffmanTable;
                Optional<HuffmanTable.TreeNode> treeNodeOptional;
                if (i == 0 && j == 0) {
                    huffmanTable = selectHt(unit, HuffmanTable.DC);
                    while (!(treeNodeOptional =
                            huffmanTable.findTreeNode(bitsBuffer.peekBits(codeLength), codeLength))
                            .isPresent()) {
                        codeLength++;
                    }
                    bitsBuffer.skipBits(codeLength);

                    HuffmanTable.TreeNode treeNode = treeNodeOptional.get();
                    bitPool.writeBits(treeNode.getBitCode(), codeLength);
                    int weight = treeNode.getWeight();

                    if (weight > 0) {
                        int value = bitsBuffer.readBits(weight);
                        bitPool.writeBits(value, weight);
                        //It will be useful when i decide to writeBit a jpeg decoder, but not now..
                        value = decipher(value, weight);
                        int prev = prevDc[unit < 4 ? 0 : (unit == 4 ? 1 : 2)];
                        value = value * quantTable.getQuant()[i][j] + prev;
                        value += prev;
                        prevDc[unit < 4 ? 0 : (unit == 4 ? 1 : 2)] = value;
                    }
                    block[i][j] = prevDc[unit < 4 ? 0 : (unit == 4 ? 1 : 2)];

                } else {
                    huffmanTable = selectHt(unit, HuffmanTable.AC);
                    while (!(treeNodeOptional =
                            huffmanTable.findTreeNode(bitsBuffer.peekBits(codeLength), codeLength))
                            .isPresent()) {
                        codeLength++;
                    }
                    HuffmanTable.TreeNode treeNode = treeNodeOptional.get();
                    int weight = treeNode.getWeight();

                    if (weight == 0) {
                        bitPool.writeBits(treeNode.getBitCode(), codeLength);
                        bitsBuffer.skipBits(codeLength);
                        return block;
                    }

                    int zeroNum = (weight >> 4) & 0xf;
                    int bitNum = weight & 0xf;

                    j += zeroNum;
                    i += j / 8;
                    j = j % 8;
                    if (i > 7) {
                        break;
                    }

                    bitPool.writeBits(treeNode.getBitCode(), treeNode.getLength());
                    bitsBuffer.skipBits(codeLength);
                    int value = bitsBuffer.readBits(bitNum);

                    if (unit < 4 && (value > 1 || value < -1)) {
                        if (lsbDataAction instanceof LSBDataProducer) {
                            LSBDataProducer producer = (LSBDataProducer) lsbDataAction;
                            int in = producer.get();
                            if (in == 0) {
                                value &= (-1 << 1);
                            } else if (in == 1) {
                                value |= 1;
                            }
                            bitPool.writeBits(value, bitNum);
                        } else {
                            LSBDataReceiver receiver = (LSBDataReceiver) lsbDataAction;
                            int out = value & 0x1;
                            if (receiver.canWrite()) {
                                receiver.writeBit(out, true);
                            }
                        }
                    } else {
                        bitPool.writeBits(value, bitNum);
                    }

                    value = decipher(value, bitNum);
                    value = value * quantTable.getQuant()[i][j];

                    block[i][j] = value;
                }
            }
        }
        return block;
    }


    private void initProperties() {
        yC = colorComponents.get(0);
        crC = colorComponents.get(1);
        cbC = colorComponents.get(2);

        for (HuffmanTable huffmanTable : huffmanTables) {
            if (huffmanTable.getType() == HuffmanTable.DC) {
                if (huffmanTable.getId() == yC.getDcId()) {
                    yDcHuffmanTable = huffmanTable;
                } else {
                    crCbDcHuffmanTable = huffmanTable;
                }
            } else {
                if (huffmanTable.getId() == yC.getDcId()) {
                    yAcHuffmanTable = huffmanTable;
                } else {
                    crCbAcHuffmanTable = huffmanTable;
                }
            }
        }
    }


    private HuffmanTable selectHt(int i, int type) {
        if (type == HuffmanTable.DC) {
            if (i < yC.getHs() * yC.getVs()) {
                return yDcHuffmanTable;
            } else {
                return crCbDcHuffmanTable;
            }
        } else {
            if (i < yC.getHs() * yC.getVs()) {
                return yAcHuffmanTable;
            } else {
                return crCbAcHuffmanTable;
            }
        }
    }

    private QuantTable selectQuantTable(int i) {
        if (i < yC.getHs() * yC.getVs()) {
            return quantTables.get(0);
        } else {
            return quantTables.get(1);
        }
    }

    private int decipher(int value, int length) {
        if (length == 0) return 0;
        int codeNum = (int) Math.pow(2, length);
        if (value < codeNum / 2)
            return -(codeNum - 1) + value;
        else
            return value;
    }
}
