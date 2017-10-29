package me.cizezsy.data;

import me.cizezsy.bit.BitMap;
import me.cizezsy.exception.BitIOException;

public class LSBDataProducer implements LSBDataAction {

    private BitMap bitMap;

    public LSBDataProducer(byte[] data) {
        int length = data.length * 8;
        StringBuilder bitStringBuilder = new StringBuilder(Integer.toBinaryString(length));
        while (bitStringBuilder.length() < 32) {
            bitStringBuilder.insert(0, "0");
        }
        byte[] actualData = new byte[data.length + 4];
        actualData[0] = (byte) Integer.parseInt(bitStringBuilder.substring(0, 8), 2);
        actualData[1] = (byte) Integer.parseInt(bitStringBuilder.substring(8, 16), 2);
        actualData[2] = (byte) Integer.parseInt(bitStringBuilder.substring(16, 24), 2);
        actualData[3] = (byte) Integer.parseInt(bitStringBuilder.substring(24, 32), 2);

        System.arraycopy(data, 0, actualData, 4, data.length);
        bitMap = new BitMap(actualData);
    }

    public int get() throws BitIOException {
        return bitMap.readBits(1);
    }

    public void position(int position) throws BitIOException {
        bitMap.position(position);
    }

    public int position() {
        return bitMap.position();
    }
}
