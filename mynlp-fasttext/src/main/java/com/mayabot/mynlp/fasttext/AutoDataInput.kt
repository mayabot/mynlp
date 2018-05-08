package com.mayabot.mynlp.fasttext

import java.io.*
import java.nio.ByteOrder
import java.util.*

fun File.openAutoDataInput(): AutoDataInput {
    return AutoDataInput.open(this)
}

class AutoDataInput(val dataInput: DataInput, val order: ByteOrder = ByteOrder.BIG_ENDIAN) : DataInput by dataInput {

    companion object {
        fun open(file: File): AutoDataInput {
            return AutoDataInput(DataInputStream(file.inputStream().buffered()))
        }
    }


    fun readShortArray(target: ShortArray) {
        for (i in 0 until target.size) {
            val b = this.readByte()
            target[i] = ByteUtils.byte2UInt(b)
        }
    }


    fun readFloatArray(target: FloatArray) {
        for (i in 0 until target.size) {
            target[i] = this.readFloat()
        }
    }

    fun loadFloatMatrix(): FloatMatrix {
        val rows = readLong().toInt()
        val cols = readLong().toInt()
        val length = rows * cols


        val matrix = FloatArray(length)

        for (i in 0 until length) {
            matrix[i] = this.readFloat()
        }
        return FloatArrayMatrix(rows, cols, matrix)
    }

    private var utf = ByteArray(256)

    @Throws(IOException::class)
    override fun readUTF(): String {
        var i = 0
        var len = utf.size
        var b = readByte()
        while (b.toInt() != 0) {
            utf[i++] = b
            if (i == len) {
                utf = Arrays.copyOf(utf, utf.size * 2)
                len = utf.size
            }
            b = readByte()
        }

        return if (i > 0) {
            String(utf, 0, i)
        } else {
            ""
        }
    }

    override fun readInt(): Int {
        return if (order == ByteOrder.LITTLE_ENDIAN) {
            val ch4 = dataInput.readUnsignedByte()
            val ch3 = dataInput.readUnsignedByte()
            val ch2 = dataInput.readUnsignedByte()
            val ch1 = dataInput.readUnsignedByte()
            if (ch1 or ch2 or ch3 or ch4 < 0)
                throw EOFException()
            (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
        } else {
            dataInput.readInt()
        }
    }

    @Throws(IOException::class)
    override fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    @Throws(IOException::class)
    override fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    private val readBuffer = ByteArray(8)
    @Throws(IOException::class)
    override fun readLong(): Long {
        return if (order == ByteOrder.LITTLE_ENDIAN) {
            readFully(readBuffer, 0, 8)

            ByteUtils.readLITTLELong(readBuffer)
        } else {
            dataInput.readLong()
        }
    }

}