package com.mayabot.nlp.fasttext.blas

import com.mayabot.nlp.fasttext.blas.vector.Vector
import java.io.File
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.min


/**
 * 矩阵和向量相乘，结果保存到target向量里面
 */
fun matrixMulVector(matrix: FloatMatrix, v: Vector, target: Vector) {
    check(matrix.rows() == target.length())
    check(matrix.cols() == v.length())

    val m_ = matrix.rows()
    for (i in 0 until m_) {
        var x = 0f
        for (j in 0 until matrix.cols()) {
            x += matrix[i, j] * v[j]
        }
        target[i] = x
    }
}


/**
 * Float矩阵
 */
interface FloatMatrix : Serializable {
    fun rows(): Int
    fun cols(): Int
    operator fun get(row: Int): Vector
    operator fun get(i: Int, j: Int): Float

    operator fun set(i: Int, j: Int, v: Float)
    fun fill(v: Float)
    fun uniform(a: Number)

    fun addVectorToRow(vector: Vector, rows: Int, a: Float)

    fun write(channel: FileChannel)

    /**
     * 矩阵的第i行和vec进行点积计算
     */
    fun dotRow(vec: Vector, i: Int): Float

    fun save(file: File) {
        file.outputStream().channel.use {
            it.writeInt(rows())
            it.writeInt(cols())
            this.write(it)
        }
    }

    companion object {
        fun byteBufferMatrix(rows: Int, cols: Int) = ByteBufferMatrix(rows, cols, false)
        fun directByteBufferMatrix(rows: Int, cols: Int) = ByteBufferMatrix(rows, cols, true)
        fun floatArrayMatrix(rows: Int, cols: Int) = FloatArrayMatrix(rows, cols)
        fun readOnlyFloatArrayMatrix(rows: Int, cols: Int, data: FloatArray) = FloatArrayMatrix(rows, cols, data)


        fun loadMatrix(file: File, mmap: Boolean): FloatMatrix {
            fun pages(total: Long, size: Int): Int = ((total + size.toLong() - 1) / size.toLong()).toInt()
            return if (mmap) {
                file.inputStream().channel.use {
                    val rows = it.readInt()
                    val cols = it.readInt()

                    //一个区域可以容纳多少行
                    var areaRows = 0
                    while (areaRows * cols < 268435456) {
                        areaRows += 10
                    }

                    val fileSize = it.size()
                    val arrayBytes = fileSize - 8
                    val areaCount = pages(arrayBytes, 4 * areaRows * cols)
                    val areaBytes = areaRows * cols * 4
                    val lastBytes = arrayBytes % (areaRows * cols * 4)

                    val list = ArrayList<ByteBuffer>()
                    for (a in 0 until areaCount) {
                        val len = if (a == areaCount - 1) lastBytes else areaBytes.toLong()
                        list += it.map(FileChannel.MapMode.READ_ONLY, 8 + a.toLong() * areaBytes, len)
                    }
                    AreaByteBufferMatrix(rows, cols, list)
                }
            } else {
                val dataInput = AutoDataInput.open(file)
                val rows = dataInput.readInt()
                val cols = dataInput.readInt()
                val floatArray = FloatArray(rows * cols)
                for (i in 0 until rows * cols) {
                    floatArray[i] = dataInput.readFloat()
                }
                FloatMatrix.readOnlyFloatArrayMatrix(rows, cols, floatArray)
            }

        }

        private fun FileChannel.writeInt(value: Int) {
            this.write(ByteBuffer.allocate(4).putInt(value).apply { flip() })
        }

        private fun FileChannel.readInt(): Int {
            val b = ByteBuffer.allocate(4)
            this.read(b)
            b.flip()
            return b.int
        }
    }
}


abstract class BaseMatrix(val rows: Int, val cols: Int) : FloatMatrix {

    override fun dotRow(vec: Vector, i: Int): Float {
        return this[i] * vec
    }

    override fun rows(): Int {
        return rows
    }

    override fun cols(): Int {
        return cols
    }

    override fun toString(): String {
        if (rows == 0) {
            return ""
        }

        val b = StringBuilder()

        b.append("-".repeat(cols * 12))
        b.append("\n")

        for (i in 0 until min(20, rows)) {
            val row = get(i)

            for (j in 0 until cols) {
                b.append(row[j]).append("\t")
            }

            b.append("\n")
        }

        if (rows > 20) {
            b.append("....more....")
        }
        b.append("\n")

        return b.toString()
    }
}
