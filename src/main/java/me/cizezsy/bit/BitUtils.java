package me.cizezsy.bit;

import me.cizezsy.exception.BitIOException;

public class BitUtils {

    public static int getBit(byte b, int pos) {
        return ((b & 0xff) >> (7 - pos)) & 1;
    }

    public static byte writeBit(byte b, int pos, int value) throws BitIOException {
        if (value != 1 && value != 0)
            throw new BitIOException("writeBit bit with a wrong value");
        if (value == 0) {
            b = (byte) ((b & 0xff) & ~(1 << (7 - pos)));
        } else {
            b = (byte) ((b & 0xff) | (1 << (7 - pos)));
        }
        return b;
    }
}
