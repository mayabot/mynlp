package common;
 
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class DirectByteBufferTest {
    public static void main(String[] args) throws InterruptedException{
            //分配128MB直接内存
        ByteBuffer bb = ByteBuffer.allocateDirect(1024*1024*128);
        bb.clear();



         
        TimeUnit.SECONDS.sleep(10);

        System.out.println("ok");
    }
}