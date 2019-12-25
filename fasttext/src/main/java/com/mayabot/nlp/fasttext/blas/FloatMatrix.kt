package com.mayabot.nlp.fasttext.blas

import com.mayabot.nlp.fasttext.blas.vector.ByteBufferVector
import com.mayabot.nlp.fasttext.blas.vector.FloatArrayVector
import com.mayabot.nlp.fasttext.blas.vector.Vector
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*

/**
 * 行存储的只读矩阵。内存实现
 */
class FloatArrayMatrix(rows: Int,
                       cols: Int,
                       val data: FloatArray) : BaseMatrix(rows, cols), FloatMatrix {


    override fun addVectorToRow(vector: Vector, i: Int, a: Float) {
        var p = i * cols
        for (j in 0 until cols) {
            data[p++] += a * vector[j]
        }
    }

    private val length = rows * cols

    private val rnd: Random = Random(0)

    //private val rowview = (0 until rows).mapIndexed { index, _ -> FloatArrayVector(data, index * cols, cols) }.toTypedArray()

    constructor() : this(0, 0)

    constructor(rows: Int, cols: Int) : this(rows, cols, FloatArray(rows * cols))

    override fun write(channel: FileChannel) {
        val byteBuffer = ByteBuffer.allocateDirect(cols * 4)
        val asFloatBuffer = byteBuffer.asFloatBuffer()

        for (row in 0 until rows) {
            asFloatBuffer.clear()
            asFloatBuffer.put(data, row * cols, cols)

            byteBuffer.position(0)
            byteBuffer.limit(cols * 4)

            channel.write(byteBuffer)
        }
    }

    override fun uniform(a_: Number) {
        val a = a_.toFloat()
        val lower = -a
        for (i in 0 until length) {
            data[i] = rnd.nextFloat() * (a - lower) + lower
        }
    }

    /**
     * 均值为0
     * @param sd 标准差
     */
    fun gaussRandom(sd: Number) {
        var sd = sd.toFloat()
        for (i in 0 until length) {
            data[i] = (rnd.nextGaussian() * sd).toFloat()
        }
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return FloatArrayVector(data, row * cols, cols)
    }

    override operator fun get(i: Int, j: Int): Float {
        return data[i * cols + j]
    }

    override operator fun set(i: Int, j: Int, v: Float) {
        data[i * cols + j] = v
    }

    override fun fill(v: Float) {
        for (i in 0 until length) {
            data[i] = v
        }
    }
}

/**
 * 底层是一个ByteBuffer
 */
class ByteBufferMatrix(rows: Int, cols: Int, val data: ByteBuffer) : BaseMatrix(rows, cols), FloatMatrix {
    override fun addVectorToRow(vector: Vector, rows: Int, a: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var length = rows * cols


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

    private val rnd: Random = Random()

    private val rowview = (0 until rows).mapIndexed { index, _ -> ByteBufferVector(data, index * cols, cols) }.toTypedArray()


    private fun index(i: Int, j: Int): Int {
        return i * cols + j
    }

    override fun uniform(a: Number) {
        var a = a.toFloat()
        val lower = -a
        for (i in 0 until length step 4) {
            data.putFloat(i, rnd.nextFloat() * (a - lower) + lower)
        }
    }

    /**
     * 均值为0
     * @param sd 标准差
     */
    fun gaussRandom(sd: Number) {
        var sd = sd.toFloat()
        for (i in 0 until length step 4) {
            data.putFloat(i, (rnd.nextGaussian() * sd).toFloat())
        }
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return ByteBufferVector(data, row * cols, cols)
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


    override fun fill(v: Float) {
        for (i in 0 until length step 4) {
            data.putFloat(i, v)
        }
    }

    override fun write(channel: FileChannel) {
        data.position(0)
        data.limit(data.capacity())

        channel.write(data)
    }

}


/**
 * 特殊版本的只读二维矩阵。
 * 在使用内存映射读取文件时，
 * Java规定每个ByteBuffer不能超过2G大小
 */
class AreaByteBufferMatrix(rows: Int, cols: Int, val data: List<ByteBuffer>) : BaseMatrix(rows, cols), FloatMatrix {
    override fun addVectorToRow(vector: Vector, rows: Int, a: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    // private var length = rows * cols
    //private val rowView = (0 until rows).mapIndexed { index, _ -> VectorDefault(data, index * cols, cols) }.toTypedArray()

    val areaRows = data[0].capacity() / 4 / cols

    private fun index(i: Int, j: Int): Int {
        return i * cols + j
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        val area = row / areaRows
        val areaOffeet = row % areaRows
        return ByteBufferVector(data[area], areaOffeet * cols, cols)
    }

    override operator fun get(i: Int, j: Int): Float {

        val area = i / areaRows
        val areaOffeet = i % areaRows

        return data[area].getFloat(index(areaOffeet, j) shl 2)
    }

    override fun write(channel: FileChannel) {
        TODO("not implemented")
//        for (x in data) {
//            x.position(0)
//            x.limit(x.capacity())
//            channel.write(x)
//        }
    }

    override fun set(i: Int, j: Int, v: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fill(v: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun uniform(a: Number) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
