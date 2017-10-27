package me.cizezsy.jpeg;

import me.cizezsy.JPEGImageWriter;
import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.huffman.HuffmanTable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BitField;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class JPEGDataProcessor {

    ColorComponent yColorComponent;
    ColorComponent crColorComponent;
    ColorComponent cbColorComponent;

    private HuffmanTable acY;
    private HuffmanTable acCr;
    private HuffmanTable acCb;

    private HuffmanTable dcY;
    private HuffmanTable dcCr;
    private HuffmanTable dcCb;

    private int sumUnit;

    private JPEGImage jpegImage;
    private int y;
    private int cr;
    private int cb;

    public JPEGDataProcessor(JPEGImage jpegImage) throws JPEGParseException {
        this.jpegImage = jpegImage;
        init(jpegImage);
    }


    public String reveal() {
        int h0 = yColorComponent.getHorizontalSample();
        int v0 = yColorComponent.getVerticalSample();
        int maxMcuX = (jpegImage.getSof0().getWidth() + 8 * h0 - 1) / (8 * h0);
        int maxMcuY = (jpegImage.getSof0().getHeight() + 8 * v0 - 1) / (8 * v0);
        int numOfBit = 0;
        int tempByte = 0;
        byte[] result = new byte[0];

        int[] imageData = jpegImage.getImageData().getData();
        int position = 0;
        end:
        for (int i = 0; i < maxMcuX; i++) {
            for (int j = 0; j < maxMcuY; j++) {
                for (int unit = 0; unit < y + cr + cb; unit++) {
                    int[][] matrix = new int[8][8];
                    completedDataUnit:
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            int nextPosition = position + 1;
                            if (x == 0 && y == 0) {
                                Optional<HuffmanTable.TreeNode> treeNode;
                                while (!(treeNode = selectHuffmanTable(unit, HuffmanTable.DC)
                                        .findTreeNode(bit2int(imageData, position, nextPosition), nextPosition - position)).isPresent()) {
                                    nextPosition++;
                                }
                                int weight = treeNode.get().getWeight();
                                position = nextPosition;
                                nextPosition = position + weight;
                                int value = bit2int(imageData, position, nextPosition);
                                matrix[x][y] = decipher(value, nextPosition - position);
                                position = nextPosition;
                            } else {
                                Optional<HuffmanTable.TreeNode> treeNode;
                                while (!(treeNode = selectHuffmanTable(unit, HuffmanTable.AC)
                                        .findTreeNode(bit2int(imageData, position, nextPosition), nextPosition - position)).isPresent()) {
                                    nextPosition++;
                                }
                                int weight = treeNode.get().getWeight();
                                if (weight == 0) {
                                    break completedDataUnit;
                                }
                                int num = weight >> 4;

                                y += num;
                                x = x + y / 8;
                                y = y % 8;

                                if (y > 7 || x > 7)
                                    break;

                                int bitNum = weight & 0xf;
                                position = nextPosition;
                                nextPosition = position + bitNum;
                                int value = bit2int(imageData, position, nextPosition);

                                matrix[x][y] = decipher(value, nextPosition - position);

                                if (matrix[x][y] != 0 && Math.abs(matrix[x][y]) != 1) {
                                    int bit = getBit(imageData, nextPosition - 1);
                                    tempByte = ((tempByte << 1) + bit);
                                    numOfBit++;
                                    if (numOfBit % 8 == 0 && numOfBit != 0) {
                                        result = ArrayUtils.add(result, (byte) tempByte);
                                        tempByte = 0;
                                        if (result[0] == numOfBit)
                                            break end;
                                    }
                                }
                                position = nextPosition;
                            }
                        }
                    }
                }
            }
        }
        return new String(Arrays.copyOfRange(result, 1, result.length));
    }


    public void hide(String data) throws JPEGParseException {
        int length = data.length();
        byte[] stringData = data.getBytes();
        byte[] hideData = new byte[stringData.length + 1];
        System.arraycopy(data.getBytes(), 0, hideData, 1, stringData.length);
        hideData[0] = (byte) (length * 8 + 8);
        Bits bits = new Bits(hideData);


        int h0 = yColorComponent.getHorizontalSample();
        int v0 = yColorComponent.getVerticalSample();
        int maxMcuX = (jpegImage.getSof0().getWidth() + 8 * h0 - 1) / (8 * h0);
        int maxMcuY = (jpegImage.getSof0().getHeight() + 8 * v0 - 1) / (8 * v0);

        int[] imageData = jpegImage.getImageData().getData();
        int position = 0;
        end:
        for (int i = 0; i < maxMcuX; i++) {
            for (int j = 0; j < maxMcuY; j++) {
                for (int unit = 0; unit < y + cr + cb; unit++) {
                    int[][] matrix = new int[8][8];
                    completedDataUnit:
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            int nextPosition = position + 1;
                            if (x == 0 && y == 0) {
                                Optional<HuffmanTable.TreeNode> treeNode;
                                while (!(treeNode = selectHuffmanTable(unit, HuffmanTable.DC)
                                        .findTreeNode(bit2int(imageData, position, nextPosition), nextPosition - position)).isPresent()) {
                                    nextPosition++;
                                }//30 5
                                int weight = treeNode.get().getWeight();
                                position = nextPosition;
                                nextPosition = position + weight;
                                int value = bit2int(imageData, position, nextPosition);
                                matrix[x][y] = decipher(value, nextPosition - position);
                                position = nextPosition;
                            } else {
                                Optional<HuffmanTable.TreeNode> treeNode;
                                int head = bit2int(imageData, position, nextPosition);
                                while (!(treeNode = selectHuffmanTable(unit, HuffmanTable.AC)
                                        .findTreeNode(head, nextPosition - position)).isPresent()) {
                                    nextPosition++;
                                    head = bit2int(imageData, position, nextPosition);
                                }
                                int weight = treeNode.get().getWeight();
                                if (weight == 0) {
                                    break completedDataUnit;
                                }
                                int num = weight >> 4;

                                y += num;
                                x = x + y / 8;
                                y = y % 8;

                                if (y > 7 || x > 7)
                                    break;

                                int bitNum = weight & 0xf;
                                if (head == 0xff) {
                                    nextPosition += 8;
                                }
                                position = nextPosition;
                                nextPosition = position + bitNum;
                                int value = bit2int(imageData, position, nextPosition);

                                matrix[x][y] = decipher(value, nextPosition - position);
                                if (unit < 4) {
                                    matrix[x][y] = jpegImage.getDqt().get(0).getDqtTables().get(0).getDqtTable()[x][y] * matrix[x][y];
                                } else {
                                    matrix[x][y] = jpegImage.getDqt().get(1).getDqtTables().get(0).getDqtTable()[x][y] * matrix[x][y];
                                }


                                if (unit < 4 && (matrix[x][y] > 1 || matrix[x][y] < 1)) {
                                    int b = bits.get();
                                    if (b == -1) break end;
//                                    writeBit(imageData, b, nextPosition - 1);
                                    int origin = matrix[x][y];
                                    if (b == 0) {
                                        origin &= ~1;
                                    } else {
                                        origin |= 1;
                                    }

                                    origin = origin / jpegImage.getDqt().get(0).getDqtTables().get(0).getDqtTable()[x][y];


                                    for (int t = 0; t < nextPosition - position; t++) {
                                        writeBit(imageData, (origin >> (nextPosition - position - 1)) & 1, position + t);
                                    }
                                }
                                position = nextPosition;
                            }
                        }
                    }
                }
            }
        }
        try {
            new JPEGImageWriter().write(jpegImage, "C:\\Users\\Administrator\\Desktop\\test_out.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init(JPEGImage jpegImage) throws JPEGParseException {
        List<ColorComponent> colorComponentList = jpegImage.getColorComponents();

        yColorComponent = colorComponentList.get(0);
        crColorComponent = colorComponentList.get(1);
        cbColorComponent = colorComponentList.get(2);

        Optional<HuffmanTable> acYOptional = jpegImage.getAc(yColorComponent.getAcId());
        Optional<HuffmanTable> acCrOptional = jpegImage.getAc(crColorComponent.getAcId());
        Optional<HuffmanTable> acCbOptional = jpegImage.getAc(cbColorComponent.getAcId());

        Optional<HuffmanTable> dcYOptional = jpegImage.getDc(yColorComponent.getDcId());
        Optional<HuffmanTable> dcCrOptional = jpegImage.getDc(crColorComponent.getDcId());
        Optional<HuffmanTable> dcCbOptional = jpegImage.getDc(cbColorComponent.getDcId());

        if (!acYOptional.isPresent() || !acCrOptional.isPresent() || !acCbOptional.isPresent()
                || !dcYOptional.isPresent() || !dcCrOptional.isPresent() || !dcCbOptional.isPresent())
            throw new JPEGParseException("HuffmanTable is wrong");

        acY = acYOptional.get();
        acCr = acCrOptional.get();
        acCb = acCbOptional.get();

        dcY = dcYOptional.get();
        dcCr = dcCrOptional.get();
        dcCb = dcCbOptional.get();

        y = yColorComponent.getHorizontalSample() * yColorComponent.getVerticalSample();
        cr = crColorComponent.getHorizontalSample() * crColorComponent.getVerticalSample();
        cb = cbColorComponent.getHorizontalSample() * cbColorComponent.getVerticalSample();
    }


    private HuffmanTable selectHuffmanTable(int index, int type) {

        index %= (y + cr + cb);
        if (index < y) {
            if (type == HuffmanTable.AC)
                return acY;
            else
                return dcY;
        } else if (index < y + cr) {
            if (type == HuffmanTable.AC)
                return acCr;
            else
                return dcCr;
        } else {
            if (type == HuffmanTable.AC)
                return acCb;
            else
                return dcCb;
        }
    }

    private Pair<Integer, Integer> zigZag(int x, int y) {
        int[] zig = {
                0, 1, 8, 16, 9, 2, 3, 10,
                17, 24, 32, 25, 18, 11, 4, 5,
                12, 19, 26, 33, 40, 48, 41, 34,
                27, 20, 13, 6, 7, 14, 21, 28,
                35, 42, 49, 56, 57, 50, 43, 36,
                29, 22, 15, 23, 30, 37, 44, 51,
                58, 59, 52, 45, 38, 31, 39, 46,
                53, 60, 61, 54, 47, 55, 62, 63,
        };
        int value = zig[x * 8 + y];
        return new ImmutablePair<>(value / 8, value % 8);
    }

    private void writeBit(int[] imageData, int value, int position) {
        if (value == 0) {
            imageData[position / 8] &= (~(1 << (7 - position % 8)));
        } else if (value == 1) {
            imageData[position / 8] |= (1 << (7 - position % 8));
        }
    }


    private int getBit(int[] imageData, int position) {
        return (imageData[position / 8] >> (7 - position % 8)) & 0b01;
    }

//    public void testAlg() {
//        HuffmanTable dcTable = new HuffmanTable(0, 0, null, null);
//        List<HuffmanTable.TreeNode> dcTreeNodes = new ArrayList<>();
//        dcTreeNodes.add(new HuffmanTable.TreeNode(2, 0, 0));
//        dcTreeNodes.add(new HuffmanTable.TreeNode(2, 1, 1));
//        dcTreeNodes.add(new HuffmanTable.TreeNode(2, 0b10, 2));
//        dcTreeNodes.add(new HuffmanTable.TreeNode(3, 0b110, 7));
//        dcTreeNodes.add(new HuffmanTable.TreeNode(4, 0b1110, 0x1e));
//        dcTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11110, 0x2e));
//        dcTable.setTreeNodes(dcTreeNodes);
//
//        HuffmanTable acTable = new HuffmanTable(0, 0, null, null);
//        List<HuffmanTable.TreeNode> acTreeNodes = new ArrayList<>();
//        acTreeNodes.add(new HuffmanTable.TreeNode(2, 0, 0));
//        acTreeNodes.add(new HuffmanTable.TreeNode(2, 0b1, 0x01));
//        acTreeNodes.add(new HuffmanTable.TreeNode(3, 0b100, 0x11));
//        acTreeNodes.add(new HuffmanTable.TreeNode(3, 0b101, 0x02));
//        acTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11000, 0x21));
//        acTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11001, 0x03));
//        acTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11010, 0x31));
//        acTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11011, 0x41));
//        acTreeNodes.add(new HuffmanTable.TreeNode(5, 0b11100, 0x12));
//        acTreeNodes.add(new HuffmanTable.TreeNode(6, 0b111010, 0x51));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1110110, 0x61));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1110111, 0x71));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1111000, 0x81));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1111001, 0x91));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1111010, 0x22));
//        acTreeNodes.add(new HuffmanTable.TreeNode(7, 0b1111011, 0x13));
//        acTreeNodes.add(new HuffmanTable.TreeNode(8, 0b11111011, 0x32));
//        acTable.setTreeNodes(acTreeNodes);
//        int[] data = {0xD3, 0x5E, 0x6E, 0x4D, 0x35, 0xf5, 0x8A};
//
//
//        int position = 0;
//        int[][] matrix = new int[8][8];
//        completedDataUnit:
//        for (int x = 0; x < 8; x++) {
//            for (int y = 0; y < 8; y++) {
//                int nextPosition = position + 1;
//                if (x == 0 && y == 0) {
//                    Optional<HuffmanTable.TreeNode> treeNode;
//                    while (!(treeNode = dcTable
//                            .findTreeNode(bit2int(data, position, nextPosition), nextPosition - position)).isPresent()) {
//                        nextPosition++;
//                    }
//                    int weight = treeNode.get().getWeight();
//                    position = nextPosition;
//                    nextPosition = position + weight;
//                    int value = bit2int(data, position, nextPosition);
//                    matrix[x][y] = decipher(value, nextPosition - position);
//                    position = nextPosition;
//                } else {
//                    Optional<HuffmanTable.TreeNode> treeNode;
//                    while (!(treeNode = acTable
//                            .findTreeNode(bit2int(data, position, nextPosition), nextPosition - position)).isPresent()) {
//                        nextPosition++;
//                    }
//                    int weight = treeNode.get().getWeight();
//                    if (weight == 0) {
//                        break completedDataUnit;
//                    }
//                    int num = weight >> 4;
//
//                    y += num;
//                    x = x + y / 8;
//                    y = y % 8;
//
//                    if (y > 7 || x > 7) break;
//
//                    int bitNum = weight & 0xf;
//                    position = nextPosition;
//                    nextPosition = position + bitNum;
//                    int value = bit2int(data, position, nextPosition);
//
//                    matrix[x][y] = decipher(value, nextPosition - position);
//                    position = nextPosition;
//                }
//            }
//        }
//    }

    public int bit2int(int[] data, int startInclusive, int endExclusive) {
        if (startInclusive == endExclusive) return 0;

        int bitResult = 0;
        for (int i = 0; startInclusive < endExclusive; i++, startInclusive++) {
            int index = startInclusive / 8;
            int position = startInclusive % 8;
            if (((data[index] >> (7 - position)) & 0b0001) == 1)
                bitResult = (bitResult << 1) + 1;
            else
                bitResult = bitResult << 1;
        }

        return bitResult;
    }


    public int decipher(int value, int length) {
        if (length == 0) return 0;
        int codeNum = (int) Math.pow(2, length);
        if (value < codeNum / 2)
            return -(codeNum - 1) + value;
        else
            return value;
    }

    //TODO Completed that
    public int unDecipher(int value, int length) {

    }

    public class Bits {
        int length;
        int position;
        byte[] data;

        public Bits(byte[] data) {
            length = data.length * 8;
            position = 0;
            this.data = data;
        }

        public int get() {
            if (position >= length) return -1;
            int result = (((data[position / 8] & 0xff) >> (7 - position % 8))) & 0b01;
            position++;
            return result;
        }
    }

}
