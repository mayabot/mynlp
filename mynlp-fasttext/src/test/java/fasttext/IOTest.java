//package fasttext;
//
//import fasttext.utils.CLangDataOutputStream;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Random;
//
//public class IOTest {
//
//    @Test()
//    public void testReadWriteFloatArray()throws IOException {
//        float[] data = new float[5];
//        Random random = new Random();
//        for (int i = 0; i < data.length; i++) {
//            data[i] = random.nextFloat();
//        }
//
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//
//        CLangDataOutputStream cout = new CLangDataOutputStream(bout);
//
//        cout.writeFloatArray(data);
//        cout.flush();
//
//        byte[] x = bout.toByteArray();
//
//        float[] data2 = new float[5];
//
//        CLangDataInputStream cin = new CLangDataInputStream(new ByteArrayInputStream(x));
//
//
//        cin.readFloatArray(data2);
//
//
//        Assert.assertTrue(Arrays.equals(data, data2));
//    }
//}

