package com.mayabot.mynlp.fasttext;


public class ByteUtils {
    public static short byte2UInt(byte b) {
        return (short)(b & 0xFF);
    }

    public static byte short2Byte(short b) {
        return (byte)b;
    }


    public static final long readLITTLELong(byte[] readBuffer) {
        return (((long)readBuffer[7] << 56) +
                ((long)(readBuffer[6] & 255) << 48) +
                ((long)(readBuffer[5] & 255) << 40) +
                ((long)(readBuffer[4] & 255) << 32) +
                ((long)(readBuffer[3] & 255) << 24) +
                ((readBuffer[2] & 255) << 16) +
                ((readBuffer[1] & 255) <<  8) +
                ((readBuffer[0] & 255) <<  0));
    }


    public static void main(String[] args) {
        for (int i = 0; i < 256; i++) {
            byte b = short2Byte((short) i);
            short x = byte2UInt(b);
            System.out.println(b+ " = "+x);
        }
    }
}
