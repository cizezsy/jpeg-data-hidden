package me.cizezsy.bit;

import com.google.common.primitives.Bytes;
import me.cizezsy.exception.BitIOException;

import java.util.LinkedList;
import java.util.List;

public class BitPool {

    private List<Byte> bytes;
    private int buffer;
    private int bufferLength;

    public BitPool() {
        this.bytes = new LinkedList<>();
    }

    public void writeBit(int value, boolean isRaw) throws BitIOException {
        if (value != 0 && value != 1) {
            throw new BitIOException("value must be 1 or 0");
        }
        buffer = (buffer << 1) + value;
        bufferLength++;
        if (bufferLength == 8) {
            bytes.add((byte) buffer);
            if (buffer == 0xff && !isRaw) {
                bytes.add((byte) 0x00);
            }
            buffer = 0;
            bufferLength = 0;
        }

    }

    public void writeBit(int value) throws BitIOException {
        writeBit(value, false);
    }


    public void writeBits(int value, int length, boolean isRaw) throws BitIOException {
        value &= ~(-1 << length);
        for (int i = 0; i < length; i++) {
            writeBit((value >> (length - i - 1)) & 1, isRaw);
        }
    }

    public void writeBits(int value, int length) throws BitIOException {

        writeBits(value, length, false);
    }

    public void writeInt8(int value) throws BitIOException {
        writeInt8(value, false);
    }

    public void writeInt8(int value, boolean isRaw) throws BitIOException {
        writeBits(value, 8, isRaw);
    }

    public void writeInt16(int value, boolean isRaw) throws BitIOException {
        writeBits(value, 16, isRaw);
    }

    public void writeInt16(int value) throws BitIOException {
        writeInt16(value, false);
    }

    public List<Byte> getBytes() {
        return bytes;
    }

    public int byteSize() {
        return bytes.size();
    }

    public int bitSize() {
        return byteSize() * 8 + bufferLength;
    }

    public byte[] toByteArray() {
        return Bytes.toArray(bytes);
    }
}
