package com.mayabot.nlp.perceptron

import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.Channel
import java.nio.channels.FileChannel
import kotlin.time.ExperimentalTime

fun readFloatArray(pSize:Int,input: DataInputStream):FloatArray{
    val parameter = FloatArray(pSize)

    val buffer = ByteArray( 1024 * 4*1024)
    val wrap = ByteBuffer.wrap(buffer)

    var point = 0

    while (true) {
        val n = input.read(buffer)
        if (n == -1) {
            break
        }
        if (n % 4 != 0) {
            throw java.lang.RuntimeException("Error Size $n")
        }
        wrap.flip()
        wrap.limit(n)

        for (i in 0 until n / 4) {

            parameter[point++] = wrap.float
        }
    }
    return parameter
}
val model = "/Users/jimichan/project-new/mynlp/mynlp-resources/mynlp-resource-pos/src/main/resources/pos-model"

@ExperimentalTime
fun main() {
    val p1 = test()



//    val t1 = System.currentTimeMillis()
//    val p1 = test()
//    val t2 = System.currentTimeMillis()
//    val p2 = test2()
//    val t3 = System.currentTimeMillis()
//    println(t2-t1)
//    println(t3-t2)
//    Assert.assertArrayEquals(p1,p2,0.00000000f)
//    println("------------")
//
//    val t4 = System.currentTimeMillis()
//    test()
//    val t5 = System.currentTimeMillis()
//    test2()
//    val t6 = System.currentTimeMillis()
//    println(t5-t4)
//    println(t6-t5)
}

fun test2():FloatArray{
    println("-------")
    val file = File(model,"parameter.bin").inputStream().channel
    return file.use { channel->
        val labelCount = channel.readInt()
        val pSize = channel.readInt()

        println("labelCount $labelCount")
        println("pSize $pSize")

        val np = channel.position()
        val size = channel.size()

        val buffer: MappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY,np,size-np)
        val floatBuffer = buffer.asFloatBuffer()

        val parameter = FloatArray(pSize)
        floatBuffer.get(parameter)

        parameter
    }

}

fun FileChannel.readInt():Int{
    val four = ByteBuffer.allocate(4)
    this.read(four)
    four.flip()
    return four.asIntBuffer().get()
}

fun test():FloatArray{
    val parameterBin = File(model,"parameter.bin").inputStream().buffered()


    return parameterBin.use { x ->
        val input = DataInputStream(x)
        val labelCount = input.readInt()
        println("labelCount $labelCount")
        val pSize = input.readInt()
        println(pSize)
        val parameter = readFloatArray(pSize,input)
        parameter
    }
}

