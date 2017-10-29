package me.cizezsy.bit;

import me.cizezsy.exception.BitIOException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitMap {
    private byte[] bits;
    private int position;
    private int length;

    public BitMap(byte[] bits) {
        this.bits = bits;
        this.length = bits.length * 8;
        this.position = 0;
    }

    public BitMap(InputStream inputStream) throws BitIOException {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            this.bits = baos.toByteArray();
            this.length = this.bits.length * 8;
            this.position = 0;
        } catch (IOException e) {
            e.printStackTrace();
            throw new BitIOException("initialize failed", e);
        }
    }


    public int readInt8() throws BitIOException {
        if (position + 8 >= length) {
            if (position == length)
                return -1;
            else
                return readBits(length - position);
        }

        if (position % 8 == 0) {
            int result = 0xff & bits[position / 8];
            position += 8;
            return result;
        }
        return readBits(8);
    }

    public int readInt16() throws BitIOException {
        if (position + 16 >= length) {
            if (position == length)
                return -1;
            else
                return readBits(length - position);
        }

        return (readInt8() << 8) | readInt8();
    }

    public int readBits(int n) throws BitIOException {
        int value = peekBits(n);
        if (value == -1) {
            position = length;
            return value;
        } else
            skipBits(n);
        return value;
    }

    public void skipBits(int n) {
        if (position + n >= length)
            position = length;
        else
            position += n;
    }

    public void skipBytes(int n) {
        skipBits(n * 8);
    }

    public int peekBits(int n) throws BitIOException {
        if (n > 32 || n < 0)
            throw new BitIOException("Not Support Operation, the bit number you want to read shouldn't large than 32 or less than zero");
        if (position + n > length) {
            if (position >= length)
                return -1;
            else
                return peekBits(length - position);
        }

        int value = 0;
        for (int i = position; i < position + n; i++) {
            int b = BitUtils.getBit(bits[i / 8], i % 8);
            value <<= 1;
            value += b;
        }
        return value;
    }


    public void write(int position, int value) throws BitIOException {
        byte raw = bits[position / 8];
        raw = BitUtils.writeBit(raw, position % 8, value);
        bits[position / 8] = raw;
    }

    public void position(int position) throws BitIOException {
        if (position > length) {
            throw new BitIOException("position out of length");
        }
        this.position = position;
    }

    public int position() {
        return position;
    }

    public int length() {
        return length;
    }

    public byte[] getBits() {
        return bits;
    }
}
