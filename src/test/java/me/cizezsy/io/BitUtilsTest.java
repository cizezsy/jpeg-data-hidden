package me.cizezsy.io;

import me.cizezsy.exception.BitIOException;
import org.junit.Assert;
import org.junit.Test;

import static me.cizezsy.bit.BitUtils.getBit;
import static me.cizezsy.bit.BitUtils.writeBit;

public class BitUtilsTest {

    @Test
    public void getBitTest() {
        byte testValue = 0b01001101;
        byte resultValue = 0;
        for (int i = 0; i < 8; i++) {
            resultValue <<= 1;
            resultValue += getBit(testValue, i);
        }
        Assert.assertEquals(testValue, resultValue);
    }

    @Test
    public void writeBitTest() throws BitIOException {
        byte testValue = 0b01001101;
        Assert.assertEquals((byte)0b01001111, writeBit(testValue, 6, 1));
        Assert.assertEquals((byte)0b11001101, writeBit(testValue, 0, 1));
    }
}
