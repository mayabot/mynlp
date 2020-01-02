package com.mayabot.nlp.fasttext.utils

import com.carrotsearch.hppc.IntArrayList
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.util.*


fun FileChannel.writeBoolean(value: Boolean) {
    this.write(ByteBuffer.allocate(2).put(if(value) 1.toByte() else 0.toByte()).apply { flip() })
}
fun FileChannel.writeInt(value: Int) {
    this.write(ByteBuffer.allocate(4).putInt(value).apply { flip() })
}

fun FileChannel.readInt(): Int {
    val b = ByteBuffer.allocate(4)
    this.read(b)
    b.flip()
    return b.int
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


operator fun ByteBuffer.set(i: Int, v: Float) {
    this.putFloat(i shl 2, v)
}



fun iota(data: IntArray) {
    for (i in 0 until data.size) {
        data[i] = i
    }
}


fun swap(array: IntArray, i: Int, j: Int) {
    val x = array[i]
    array[i] = array[j]
    array[j] = x
}

fun swap(array: IntArrayList, i: Int, j: Int) {
    val x = array.get(i)
    array.set(i, array.get(j))
    array.set(j, x)
}


fun shuffle(array: IntArray, random: Random) {
    val size = array.size
    for (i in size - 1 downTo 2) {
        swap(array, i - 1, random.nextInt(i))
    }
}

fun shuffle(array: IntArrayList, random: Random) {
    val size = array.size()
    for (i in size - 1 downTo 2) {
        swap(array, i - 1, random.nextInt(i))
    }
}


/**
 * 一共可以分为多少块。99个，2个一份，应该为50份
 */
fun pages(total: Int, size: Int): Int = (total + size - 1) / size

fun pages(total: Long, size: Int): Int = ((total + size.toLong() - 1) / size.toLong()).toInt()