package me.cizezsy.data;

import com.google.common.primitives.Bytes;
import me.cizezsy.exception.BitIOException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.LinkedList;
import java.util.List;

public class LSBDataReceiver implements LSBDataAction {
    private List<Byte> bytes;
    private int buffer;
    private int bufferLength;
    private int length = -1;

    public LSBDataReceiver() {
        bytes = new LinkedList<>();
        buffer = 0;
        bufferLength = 0;
    }

    public boolean canWrite() {
        if (length != -1 && bytes.size() - 4 >= length / 8) {
            return false;
        } else if (bytes.size() == 5) {
            length = ((bytes.get(0) & 0xff) << 24) |
                    ((bytes.get(1) & 0xff) << 16) |
                    ((bytes.get(2) & 0xff) << 8) |
                    (bytes.get(3) & 0xff);
        }
        return true;
    }

    public boolean write(int value) throws BitIOException {
        if (value != 0 && value != 1) {
            throw new BitIOException("value must be 1 or 0");
        }

        buffer = (buffer << 1) + value;
        bufferLength++;
        if (bufferLength == 8) {
            bytes.add((byte) buffer);
            buffer = 0;
            bufferLength = 0;
        }

        return true;
    }

    public byte[] recoverData() throws BitIOException {
        if (bytes.size() <= 4)
            throw new BitIOException("data recover failed! data length is not recover");
        return Bytes.toArray(bytes.subList(4, bytes.size()));
    }

}
