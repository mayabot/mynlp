package com.mayabot.nlp.fasttext.blas

import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.readInt
import com.mayabot.nlp.fasttext.utils.writeInt
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*


fun loadDenseMatrix(dataInput: AutoDataInput): DenseMatrix {
    val rows = dataInput.readInt()
    val cols = dataInput.readInt()
    val floatArray = FloatArray(rows * cols)
    for (i in 0 until rows * cols) {
        floatArray[i] = dataInput.readFloat()
    }
    return floatArrayMatrix(rows, cols, floatArray)
}

fun loadDenseMatrix(inputStream: InputStream?): DenseMatrix {
    val dataInput = AutoDataInput(DataInputStream(inputStream))
    return loadDenseMatrix(dataInput)
}

fun loadDenseMatrix(file: File, mmap: Boolean): DenseMatrix {
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
        floatArrayMatrix(rows, cols, floatArray)
    }
}

fun loadFloatArrayMatrix(dataInput: AutoDataInput): DenseArrayMatrix {
    val rows = dataInput.readInt()
    val cols = dataInput.readInt()
    val floatArray = FloatArray(rows * cols)
    for (i in 0 until rows * cols) {
        floatArray[i] = dataInput.readFloat()
    }
    return floatArrayMatrix(rows, cols, floatArray)
}

fun loadFloatArrayMatrixCPP(dataInput: AutoDataInput): DenseArrayMatrix {
    val rows = dataInput.readLong().toInt()
    val cols = dataInput.readLong().toInt()
    val floatArray = FloatArray(rows * cols)
    for (i in 0 until rows * cols) {
        floatArray[i] = dataInput.readFloat()
    }
    return floatArrayMatrix(rows, cols, floatArray)
}


/**
 * 行存储的只读矩阵。内存实现
 */
class DenseArrayMatrix(row: Int,
                       col: Int,
                       val data: FloatArray) : BasicDenseMatrix(row, col) {

    private val length = row * col

    private val rnd: Random = Random(0)

    constructor() : this(0, 0)

    constructor(rows: Int, cols: Int) : this(rows, cols, FloatArray(rows * cols))

    override fun zero() {
        for (i in 0 until length) {
            data[i] = 0f
        }
    }

    /**
     * 随机 -[number] -> [number] 均匀分布的填充
     */
    override fun uniform(number: Number) {
        val a = number.toFloat()
        val lower = -a
        for (i in 0 until length) {
            data[i] = rnd.nextFloat() * (a - lower) + lower
        }
    }

    override fun save(channel: FileChannel) {

        fun write(channel: FileChannel) {
            val byteBuffer = ByteBuffer.allocateDirect(col * 4)
            val asFloatBuffer = byteBuffer.asFloatBuffer()

            for (row in 0 until row) {
                asFloatBuffer.clear()
                asFloatBuffer.put(data, row * col, col)

                byteBuffer.position(0)
                byteBuffer.limit(col * 4)

                channel.write(byteBuffer)
            }
        }

        channel.writeInt(row)
        channel.writeInt(col)
        write(channel)

    }

    override fun save(file: File) {

//        fun write(channel: FileChannel) {
//            val byteBuffer = ByteBuffer.allocateDirect(col * 4)
//            val asFloatBuffer = byteBuffer.asFloatBuffer()
//
//            for (row in 0 until row) {
//                asFloatBuffer.clear()
//                asFloatBuffer.put(data, row * col, col)
//
//                byteBuffer.position(0)
//                byteBuffer.limit(col * 4)
//
//                channel.write(byteBuffer)
//            }
//        }
//
//        file.outputStream().channel.use { channel ->
//            channel.writeInt(row)
//            channel.writeInt(col)
//            write(channel)
//        }

        file.outputStream().channel.use { channel ->
            save(channel)
        }

    }

    /**
     * 均值为0
     * @param sd 标准差
     */
    fun gaussRandom(number: Number) {
        val sd = number.toFloat()
        for (i in 0 until length) {
            data[i] = (rnd.nextGaussian() * sd).toFloat()
        }
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return DenseVector(data, row * col, col)
    }

    override operator fun get(i: Int, j: Int): Float {
        return data[i * col + j]
    }

    override operator fun set(i: Int, j: Int, v: Float) {
        data[i * col + j] = v
    }


}

/**
 * 底层是一个ByteBuffer
 */
class ByteBufferMatrix(row: Int, col: Int, val data: ByteBuffer) : BasicDenseMatrix(row, col) {

    private val length = row * col

    private val rnd: Random = Random()

    constructor() : this(0, 0)

    //TODO direct的bytebuffer 怎么回收
    constructor(rows: Int, cols: Int, direct: Boolean = true)
            : this(
            rows,
            cols,
            if (direct)
                ByteBuffer.allocateDirect((rows * cols) shl 2)
            else
                ByteBuffer.allocate((rows * cols) shl 2)
    )


    // impl Dense Matrix

    override fun zero() {
        for (i in 0 until length step 4) {
            data.putFloat(i, 0f)
        }
    }


    override fun uniform(number: Number) {
        val a = number.toFloat()
        val lower = -a
        for (i in 0 until length step 4) {
            data.putFloat(i, rnd.nextFloat() * (a - lower) + lower)
        }
    }

    /**
     * get cell
     */
    override operator fun get(i: Int, j: Int): Float {
        return data.getFloat(index(i, j) shl 2)
    }

    /**
     * set cell
     */
    override operator fun set(i: Int, j: Int, v: Float) {
        data.putFloat(index(i, j) shl 2, v)
    }

    override fun save(file: File) {
        file.outputStream().channel.use { channel ->
            save(channel)
        }
    }

    override fun save(channel: FileChannel) {

        fun write(channel: FileChannel) {
            data.position(0)
            data.limit(data.capacity())

            channel.write(data)
        }

        channel.writeInt(row)
        channel.writeInt(col)
        write(channel)
    }

    private fun index(i: Int, j: Int): Int {
        return i * col + j
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return ByteBufferDenseVector(data, row * col, col)
    }


}


/**
 * 特殊版本的只读二维矩阵。
 * 在使用内存映射读取文件时，
 * Java规定每个ByteBuffer不能超过2G大小
 */
class AreaByteBufferMatrix(row: Int, col: Int, val data: List<ByteBuffer>) : BasicDenseMatrix(row, col) {

    // private var length = rows * cols
    //private val rowView = (0 until rows).mapIndexed { index, _ -> VectorDefault(data, index * cols, cols) }.toTypedArray()

    val areaRows = data[0].capacity() / 4 / col

    private fun index(i: Int, j: Int): Int {
        return i * col + j
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        val area = row / areaRows
        val areaOffset = row % areaRows
        return ByteBufferDenseVector(data[area], areaOffset * col, col)
    }

    override operator fun get(i: Int, j: Int): Float {

        val area = i / areaRows
        val areaOffeet = i % areaRows

        return data[area].getFloat(index(areaOffeet, j) shl 2)
    }

    override fun zero() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(file: File) {
        TODO("not implemented")
    }

    override fun save(channel: FileChannel) {
        TODO("Not yet implemented")
    }

    override fun set(i: Int, j: Int, v: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uniform(a: Number) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


abstract class BasicDenseMatrix(
        override val row: Int,
        override val col: Int
) : DenseMatrix {

    override fun dotRow(vec: Vector, i: Int): Float {
        return this[i] * vec
    }

    override fun addVectorToRow(vector: Vector, i: Int, a: Float) {
        this[i].plusAssign(a to vector)
//        var p = i * col
//        for (j in 0 until col) {
//            data[p++] += a * vector[j]
//        }
    }

    override fun addRowToVector(target: Vector, i: Int, a: Double?) {
        val row = this[i]
        if (a == null) {
            for (j in 0 until col) {
                target[j] += row[j]
            }
        } else {
            for (j in 0 until col) {
                target[j] += (a * row[j]).toFloat()
            }
        }
    }


    override fun multiplyRow(nums: Vector, ib: Int, ie_: Int) {
        val ie = if (ie_ == -1) row else ie_

        check(ie <= nums.length())

        for (i in ib until ie) {
            val n = nums[i - ib]
            if (n != 0f) {
                this[i] *= n
            }
        }
    }

    override fun divideRow(denoms: Vector, ib: Int, ie_: Int) {
        // divideRow
        val ie = if (ie_ == -1) row else ie_

        check(ie <= denoms.length())

        for (i in ib until ie) {
            val n = denoms[i - ib]
            if (n != 0f) {
                this[i] /= n
            }
        }
    }

    override fun l2NormRow(i: Int): Float {
        return this[i].norm2()
    }

    override fun l2NormRow(norms: Vector) {
        check(norms.length() == row)
        for (i in 0 until row) {
            norms[i] = l2NormRow(i)
        }
    }

}
