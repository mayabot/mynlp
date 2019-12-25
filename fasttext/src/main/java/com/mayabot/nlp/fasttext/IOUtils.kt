package com.mayabot.nlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.mayabot.nlp.fasttext.blas.FloatMatrix
import com.mayabot.nlp.fasttext.blas.vector.ByteUtils
import com.mayabot.nlp.fasttext.blas.vector.Vector
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files


fun FileChannel.writeBoolean(value: Boolean) {
    this.write(ByteBuffer.allocate(2).put(if(value) 1.toByte() else 0.toByte()).apply { flip() })
}
fun FileChannel.writeInt(value: Int) {
    this.write(ByteBuffer.allocate(4).putInt(value).apply { flip() })
}
fun FileChannel.writeLong(value: Long) {
    this.write(ByteBuffer.allocate(8).putLong(value).apply { flip() })
}

fun FileChannel.writeShortArray(value: ShortArray) {
    val b = ByteBuffer.allocate(value.size*2)
    for (i in 0 until value.size) {
        b.put(ByteUtils.short2Byte(value[i]))
    }
    b.flip()
    while (b.hasRemaining()) {
        write(b)
    }
}

fun FileChannel.writeDouble(value: Double) {
    this.write(ByteBuffer.allocate(8).putDouble(value).apply { flip() })
}


fun File.lines() = Files.lines(this.toPath())!!

fun File.forEachLine(action: (String) -> Unit) = Files.lines(this.toPath()).forEach(action)

fun File.firstLine(): String? = Files.lines(this.toPath()).findFirst().orElse(null)


@Throws(IOException::class)
fun ByteBuffer.writeUTF(string: String){
    string.toByteArray().forEach {
        this.put(it)
    }

    this.put(byteZero)
}

const val byteZero = 0.toByte()


fun ByteBuffer.writeFloatArray(source:FloatArray){
    for (i in 0 until source.size) {
        this.putFloat(source[i])
    }
}

inline fun IntArrayList.forEach2(action: (num: Int) -> Unit) {
    val buffer = this.buffer
    var i = 0
    val size = this.size()
    while (i < size) {
        action(buffer[i])
        i++
    }
}
