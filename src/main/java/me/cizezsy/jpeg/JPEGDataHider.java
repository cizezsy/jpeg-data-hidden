package me.cizezsy.jpeg;

import me.cizezsy.exception.JPEGParseException;
import me.cizezsy.huffman.HuffmanTable;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;

public class JPEGDataHider {

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

    public JPEGDataHider(JPEGImage jpegImage) throws JPEGParseException {
        this.jpegImage = jpegImage;
    }


    public void hide(String data) throws JPEGParseException {
        init(jpegImage);

        int h0 = yColorComponent.getHorizontalSample();
        int v0 = yColorComponent.getVerticalSample();
        int maxMcuX = (jpegImage.getSof0().getWidth() + 8 * h0 - 1) / (8 * h0);
        int maxMcuY = (jpegImage.getSof0().getHeight() + 8 * v0 - 1) / (8 * v0);

        int[] imageData = jpegImage.getImageData().getData();
        int position = 0;
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
                                        .findTreeNode(bit2int(imageData, position, nextPosition))).isPresent()) {
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
                                        .findTreeNode(bit2int(imageData, position, nextPosition))).isPresent()) {
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

                                if (y > 7 || x > 7) break;

                                int bitNum = weight & 0xf;
                                position = nextPosition;
                                nextPosition = position + bitNum;
                                int value = bit2int(imageData, position, nextPosition);

                                matrix[x][y] = decipher(value, nextPosition - position);
                                position = nextPosition;
                            }
                        }
                    }
                }
            }
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

    public int bit2int(int[] data, int startInclusive, int endExclusive) {
        if (startInclusive == endExclusive) return 0;

        BitSet bitSet = new BitSet(endExclusive - startInclusive);
        for (int i = 0; startInclusive < endExclusive; i++, startInclusive++) {
            int index = startInclusive / 8;
            int position = startInclusive % 8;
            if (((data[index] >> (7 - position)) & 0b0001) == 1)
                bitSet.set(i, true);
            else
                bitSet.set(i, false);
        }
        if (bitSet.length() == 0) return 0;
        else return (int) bitSet.toLongArray()[0];
    }


    public int decipher(int value, int length) {
        if (length == 0) return 0;
        int codeNum = (int) Math.pow(2, length);
        if (value < codeNum / 2)
            return -(codeNum - 1) + value;
        else
            return value;
    }

}
