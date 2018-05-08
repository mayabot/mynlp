package com.mayabot.mynlp.fasttext

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel


fun FileChannel.writeBoolean(value: Boolean) {
    this.write(ByteBuffer.allocate(2).put(if(value) 1.toByte() else 0.toByte()).apply { flip() })
}
fun FileChannel.writeInt(value: Int) {
    this.write(ByteBuffer.allocate(4).putInt(value).apply { flip() })
}
fun FileChannel.writeLong(value: Long) {
    this.write(ByteBuffer.allocate(8).putLong(value).apply { flip() })
}
fun FileChannel.writeFloat(value: Float) {
    this.write(ByteBuffer.allocate(4).putFloat(value).apply { flip() })
}

fun FileChannel.readInt():Int {
    val b = ByteBuffer.allocate(4)
    this.read(b)
    b.flip()
    return b.int
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


const val byteZero = 0.toByte()



@Throws(IOException::class)
fun ByteBuffer.writeUTF(string: String){
    string.toByteArray().forEach {
        this.put(it)
    }

    this.put(com.mayabot.mynlp.fasttext.byteZero)
}


fun ByteBuffer.writeFloatArray(source:FloatArray){
    for (i in 0 until source.size) {
        this.putFloat(source[i])
    }
}