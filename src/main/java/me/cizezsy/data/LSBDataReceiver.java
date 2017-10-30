package me.cizezsy.data;

import com.google.common.primitives.Bytes;
import me.cizezsy.bit.BitPool;
import me.cizezsy.exception.BitIOException;

public class LSBDataReceiver implements LSBDataAction {

    private BitPool bitPool;

    private int length = -1;

    public LSBDataReceiver() {
        bitPool = new BitPool();
    }

    public boolean canWrite() {
        if (length != -1 && bitPool.byteSize() - 4 >= length / 8) {
            return false;
        } else if (bitPool.byteSize() == 5) {
            length = ((bitPool.getBytes().get(0) & 0xff) << 24) |
                    ((bitPool.getBytes().get(1) & 0xff) << 16) |
                    ((bitPool.getBytes().get(2) & 0xff) << 8) |
                    (bitPool.getBytes().get(3) & 0xff);
        }
        return true;
    }

    public void writeBit(int value) throws BitIOException {
        bitPool.writeBit(value);
    }

    public void writeBit(int value, boolean isRaw) throws BitIOException {
        bitPool.writeBit(value, isRaw);
    }



    public byte[] recoverData() throws BitIOException {
        if (bitPool.byteSize() <= 4)
            throw new BitIOException("data recover failed! data length is not recover");
        return Bytes.toArray(bitPool.getBytes().subList(4, bitPool.byteSize()));
    }

}
