package me.cizezsy.bit;

import me.cizezsy.exception.BitIOException;

public class ImageDataBuffer {
    private BitMap bitMap;
    private int buffer;
    private int bufferLength;
    private int position;

    public ImageDataBuffer(BitMap bitMap) {
        this.bitMap = bitMap;
        this.position = bitMap.position();
        this.buffer = 0;
        this.bufferLength = 0;
    }

    public int peekBits(int length) throws BitIOException {
        if(length == 0) return 0;
        if(length < 0 || length > 24)
            throw new BitIOException("buffer peek error, length too long");

        while (bufferLength < length) {
            int value = bitMap.readBits(8);

            if (value == -1)
                break;

            if (value == 0xff) {
                value = bitMap.readBits(8);

                if (value == -1)
                    break;

                if (value == 0) {
                    value = 0xff;
                    position += 8;
                } else {
                    buffer <<= 8;
                    buffer += 0xff;
                    bufferLength += 8;
                }
            }
            buffer <<= 8;
            buffer += value;
            bufferLength += 8;
        }
        return (buffer >>> (bufferLength - length));
    }

    public void skipBits(int length) throws BitIOException {
        while (length > 24) {
            skipBits(24);
            length -= 24;
        }

        if (buffer < length) {
            peekBits(length);
        }

        position += length;
        bufferLength -= length;
        buffer &= (1 << bufferLength) - 1;
    }

    public int readBits(int length) throws BitIOException {
        int value = peekBits(length);
        skipBits(length);
        return value;
    }

    public BitMap getBitMap() {
        return bitMap;
    }

    public int getPosition() {
        return position;
    }
}
