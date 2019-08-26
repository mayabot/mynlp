package com.mayabot.nlp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Random;

public class TestFileMap {
    public static void main2(String[] args) throws Exception {
        //FileChannel.open()

        RandomAccessFile f = new RandomAccessFile("testdata/float.martix", "rw");
        FileChannel fileChannel = f.getChannel();


        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());

        buffer.getInt();
        System.out.println(buffer);
        buffer.getLong();
        System.out.println(buffer);
        buffer.getChar();
        System.out.println(buffer);


        float[] floats = new float[300];

        buffer.asFloatBuffer().get(floats);
        System.out.println(Arrays.toString(floats));

        System.out.println(buffer);

//        FloatBuffer floatBuffer = buffer.asFloatBuffer();
//
//
//        float[] floats = new float[300];
//
//        floatBuffer.position(0);
//        floatBuffer.get(floats);
//        System.out.println(floatBuffer);
//        System.out.println(Arrays.toString(floats));
//        System.out.println(buffer);
//
//
//
//        System.out.println("----"+buffer.asLongBuffer());
//        System.out.println(buffer.asFloatBuffer());
//        System.out.println(buffer.position(1200));
//        System.out.println(buffer.slice().get(0));

//        Random random = new Random();
//
//        long t1 = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; i++) {
//            floatBuffer.position(random.nextInt(1000000)*300);
//            floatBuffer.get(floats);
//        }
//        long t2 = System.currentTimeMillis();
//
//        System.out.println(t2-t1);

        //TimeUnit.SECONDS.sleep(10);


        fileChannel.close();


    }


    public static void main(String[] args) throws Exception {
        test();
    }

    public static void test() throws Exception {
        FileOutputStream fout = new FileOutputStream(new File("testdata/float.martix"));

        FileChannel channel = fout.getChannel();

        System.out.println(channel.size());

        Random random = new Random(0);


        ByteBuffer byteBuffer = ByteBuffer.allocate(300 * 4);
        FloatBuffer buffer = byteBuffer.asFloatBuffer();


        for (int i = 0; i < 10; i++) {
            buffer.clear();
            byteBuffer.clear();
            for (int j = 0; j < 300; j++) {
                buffer.put(random.nextFloat());
            }

            channel.write(byteBuffer);

        }

        channel.close();
        fout.close();
    }

}
