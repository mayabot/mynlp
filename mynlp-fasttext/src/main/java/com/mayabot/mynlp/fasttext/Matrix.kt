@file:Suppress("NAME_SHADOWING")

package com.mayabot.mynlp.fasttext


import fasttext.QMatrix
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.min

/**
 * 矩阵和向量相乘，结果保存到target向量里面
 */
fun matrixMulVector(matrix: FloatMatrix, v: Vector, target: MutableVector) {
    checkArgument(matrix.rows() == target.length())
    checkArgument(matrix.cols() == v.length())

    val m_ = matrix.rows()
    for (i in 0 until m_) {
        var x = 0f
        for (j in 0 until matrix.cols()) {
            x += matrix[i, j] * v[j]
        }
        target[i] = x
    }
}

fun matrixMulVector(matrix: QMatrix, v: Vector, target: MutableVector) {
    checkArgument(matrix.m == target.length())
    checkArgument(matrix.n == v.length())

    val m_ = matrix.m
    for (i in 0 until m_) {
        target[i] = matrix.dotRow(v,i)
    }
}

interface FloatMatrix {
    fun rows(): Int
    fun cols(): Int
    operator fun get(row: Int): Vector
    operator fun get(i: Int, j: Int): Float

    fun write(channel: FileChannel)

    companion object {
        fun byteBufferMatrix(rows: Int, cols: Int) = MutableByteBufferMatrix(rows, cols, false)
        fun directByteBufferMatrix(rows: Int, cols: Int) = MutableByteBufferMatrix(rows, cols, true)
        fun floatArrayMatrix(rows: Int, cols: Int) = MutableFloatArrayMatrix(rows, cols)
        fun readOnlyFloatArrayMatrix(rows: Int, cols: Int,data: FloatArray) = FloatArrayMatrix(rows, cols, data)

    }
}

/**
 * 可变的Matrix
 */
interface MutableFloatMatrix : FloatMatrix {
    override operator fun get(row: Int): MutableVector
    operator fun set(i: Int, j: Int, v: Float)
    fun fill(v: Float)
    fun uniform(a: Number)
}

/**
 * 只读的Float向量
 */
interface Vector {

    operator fun get(index: Int): Float

    fun length(): Int

    fun prod(v: Vector): Float

    operator fun times(v: Vector): Float

    fun norm2(): Float

    fun check()

    fun copy(): MutableVector

    fun access(call: (Int, Float) -> Unit)

    companion object {
        fun byteBufferVector(size: Int) = MutableByteBufferVector(ByteBuffer.allocate(size shl 2), 0, size)
        fun directByteBufferVector(size: Int) = MutableByteBufferVector(ByteBuffer.allocateDirect(size shl 2), 0, size)
        fun floatArrayVector(size: Int) = MutableFloatArrayVector(size)

        fun dot(a: Vector, b: Vector): Float {
            return a * b
        }

        fun cosine(a: Vector, b: Vector): Float {
            val normA = a * a
            val normB = b * b
            return if (normA == 0.0f || normB == 0.0f) {
                0.0f
            } else (a * b / Math.sqrt((normA * normB).toDouble())).toFloat()
        }
    }
}

interface MutableVector : Vector {

    fun fill(v: Number)

    fun fill(call: (Int) -> Float)

    operator fun set(index: Int, value: Float)

    operator fun plusAssign(v: Vector)

    operator fun plusAssign(x: Pair<Number, Vector>)

    operator fun minusAssign(v: Vector)

    operator fun minusAssign(x: Pair<Number, Vector>)

    operator fun timesAssign(scale: Number)

    operator fun divAssign(scale: Number)

    fun putAll(v: FloatArray)

    fun zero()

    /**
     * 赋值
     */
    operator fun invoke(v: Vector)

}

abstract class BaseMatrix(val rows: Int, val cols: Int) : FloatMatrix {

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

/**
 * 底层是一个ByteBuffer。实现只读版本的FloatMatrix
 */
class ByteBufferMatrix(rows: Int, cols: Int, val data: ByteBuffer) : BaseMatrix(rows, cols), FloatMatrix {

    // private var length = rows * cols
    //private val rowView = (0 until rows).mapIndexed { index, _ -> VectorDefault(data, index * cols, cols) }.toTypedArray()

    private fun index(i: Int, j: Int): Int {
        return i * cols + j
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return ByteBufferVector(data, row * cols, cols)
    }

    override operator fun get(i: Int, j: Int): Float {
        return data.getFloat(index(i, j) shl 2)
    }

    override fun write(channel: FileChannel) {
        data.position(0)
        data.limit(data.capacity())

        channel.write(data)
    }
}

class AreaByteBufferMatrix(rows: Int, cols: Int, val data: List<ByteBuffer>) : BaseMatrix(rows, cols), FloatMatrix {

    // private var length = rows * cols
    //private val rowView = (0 until rows).mapIndexed { index, _ -> VectorDefault(data, index * cols, cols) }.toTypedArray()

    val areaRows = data[0].capacity()/4/cols

    private fun index(i: Int, j: Int): Int {
        return i * cols + j
    }

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        val area = row/areaRows
        val areaOffeet = row%areaRows
        return ByteBufferVector(data[area], areaOffeet * cols, cols)
    }

    override operator fun get(i: Int, j: Int): Float {

        val area = i/areaRows
        val areaOffeet = i%areaRows

        return data[area].getFloat(index(areaOffeet,j) shl 2)
    }

    override fun write(channel: FileChannel) {
        for (x in data) {
            x.position(0)
            x.limit(x.capacity())
            channel.write(x)
        }
    }
}

class MutableByteBufferMatrix(rows: Int, cols: Int, direct: Boolean = true) : BaseMatrix(rows, cols), MutableFloatMatrix {

    private var length = rows * cols

    val data = if (direct) ByteBuffer.allocateDirect(length shl 2)!! else ByteBuffer.allocateDirect(length shl 2)!!

    private val rnd: Random = Random()

    private val rowview = (0 until rows).mapIndexed { index, _ -> MutableByteBufferVector(data, index * cols, cols) }.toTypedArray()

    constructor() : this(0, 0)

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
    override operator fun get(row: Int): MutableVector {
        return rowview[row]
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


open class ByteBufferVector(@JvmField protected val data: ByteBuffer,
                            @JvmField protected val offset: Int,
                            @JvmField protected val length: Int) : Vector {

    constructor(data: ByteBuffer) : this(data, 0, data.capacity() / 4)

    /**
     * index 0 until length
     */
    override fun get(index: Int): Float {
        return data.getFloat((index + offset) shl 2)
    }

    override fun length(): Int = length

    override fun times(v: Vector) = this.prod(v)

    override fun prod(v: Vector): Float {
        //checkArgument(this.length() == v.length())
        var result = 0f
        var j = 0
        for (i in offset until offset + length) {
            result += data.getFloat(i shl 2) * v[j++]
        }
        return result
    }

    override fun access(call: (Int, Float) -> Unit) {
        var j = 0
        for (i in offset shl 2 until (offset + length) * 4 step 4) {
            call(j++, data.getFloat(i))
        }
    }

    /**
     * 第二范数 || v ||
     */
    override fun norm2(): Float {
        var sum = 0.0f
        for (i in offset shl 2 until (offset + length) * 4 step 4) {
            val x = data.getFloat(i)
            sum += x * x
        }
        return Math.sqrt(sum.toDouble()).toFloat()
    }

    override fun check() {
        for (i in offset until offset + length) {
            val f = data.getFloat(i shl 2)
            checkArgument(!f.isNaN())
            checkArgument(!f.isInfinite())
        }
    }

    override fun toString(): String {
        if (length() == 0)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        val iMax = length() - 1
        val end = offset + iMax
        var i = offset
        while (true) {
            b.append(data.getFloat(i shl 2))
            if (i == end)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    override fun copy(): MutableVector {
        val dest = ByteBuffer.allocate(length shl 2)
        for (i in offset shl 2 until (offset + length) * 4) {
            dest.put(data.get(i))
        }
        return MutableByteBufferVector(dest, 0, length)
    }
}

class MutableByteBufferVector(data: ByteBuffer,
                              offset: Int, length: Int) : ByteBufferVector(data, offset, length), MutableVector {

    constructor(size: Int) : this(ByteBuffer.allocate(size shl 2), 0, size)

    override fun fill(v: Number) {
        val v = v.toFloat()
        for (i in offset * 4 until (offset + length) * 4 step 4) {
            data.putFloat(i, v)
        }
    }

    override fun fill(call: (Int) -> Float) {
        var j = 0
        for (i in offset shl 2 until (offset + length) * 4 step 4) {
            data.putFloat(call(j++))
        }
    }

    override fun zero() {
        fill(0)
    }

    override fun invoke(v: Vector) {
        checkArgument(this.length() == v.length())
        for (i in 0 until v.length()) {
            this[i] = v[i]
        }
    }

    override fun set(index: Int, value: Float) {
        data.putFloat( (index+offset) shl 2, value)
    }

    override fun putAll(v: FloatArray) {
        checkArgument(length == v.size)
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, v[j++])
        }
    }


    override fun plusAssign(v: Vector) {
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) + v[j++])
        }
    }

    override fun plusAssign(v: Pair<Number, Vector>) {
        val scale = v.first.toFloat()
        val vector = v.second
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) + vector[j++] * scale)
        }
    }

    override fun minusAssign(x: Vector) {
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) - x[j++])
        }
    }

    override fun minusAssign(x: Pair<Number, Vector>) {
        val scale = x.first.toFloat()
        val vector = x.second
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) - vector[j++] * scale)
        }
    }

    override fun timesAssign(scale: Number) {
        val scale = scale.toFloat()
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) * scale)
        }
    }

    override fun divAssign(scale: Number) {
        val scale = scale.toFloat()
        if (scale != 0f) {
            for (i in (offset * 4 until (offset + length) * 4 step 4)) {
                data.putFloat(i, data.getFloat(i) / scale)
            }
        }
    }
}

inline operator fun ByteBuffer.set(i: Int, v: Float) {
    this.putFloat(i shl 2, v)
}


/**
 * 只读版本的基于FloatArray的向量
 */
open class FloatArrayVector(@JvmField protected val data: FloatArray,
                            @JvmField protected val offset: Int,
                            @JvmField protected val length: Int) : Vector {

    constructor(data: FloatArray) : this(data, 0, data.size)

    /**
     * index 0 until length
     */
    override fun get(index: Int): Float {
        return data[index+offset]
    }

    override fun length(): Int = length

    override fun times(v: Vector) = this.prod(v)

    override fun prod(v: Vector): Float {
        //checkArgument(this.length() == v.length())
        var result = 0f
        var j = 0
        for (i in offset until offset + length) {
            result += data[i] * v[j++]
        }
        return result
    }

    override fun access(call: (Int, Float) -> Unit) {
        var j = 0
        for (i in offset until (offset + length)) {
            call(j++, data[i])
        }
    }

    /**
     * 第二范数 || v ||
     */
    override fun norm2(): Float {
        var sum = 0.0f
        for (i in offset until (offset + length)) {
            val x = data[i]
            sum += x * x
        }
        return Math.sqrt(sum.toDouble()).toFloat()
    }

    override fun check() {
        for (i in offset until offset + length) {
            val f = data[i]
            checkArgument(!f.isNaN())
            checkArgument(!f.isInfinite())
        }
    }

    override fun toString(): String {
        if (length() == 0)
            return "[]"

        val b = StringBuilder()
        b.append('[')
        val iMax = length() - 1
        val end = offset + iMax
        var i = offset
        while (true) {
            b.append(data[i])
            if (i == end)
                return b.append(']').toString()
            b.append(", ")
            i++
        }
    }

    override fun copy(): MutableVector {
        val dest = FloatArray(length)
        System.arraycopy(this.data, offset, dest, 0, length)
        return MutableFloatArrayVector(dest, 0, length)
    }
}

class MutableFloatArrayVector(data: FloatArray,
                              offset: Int, length: Int) : FloatArrayVector(data, offset, length), MutableVector {

    constructor(size: Int) : this(FloatArray(size), 0, size)

    override fun fill(v: Number) {
        val v = v.toFloat()
        for (i in offset until offset + length) {
            data[i] = v
        }
    }

    override fun fill(call: (Int) -> Float) {
        var j = 0
        for (i in offset until offset + length) {
            data[i] = call(j++)
        }
    }

    override fun zero() {
        fill(0)
    }

    override fun invoke(v: Vector) {
        checkArgument(this.length() == v.length())

        var j = offset
        for (i in 0 until v.length()) {
            data[j++] = v[i]
        }
    }

    override fun set(index: Int, value: Float) {
        data[index+offset] = value
    }

    override fun putAll(v: FloatArray) {
        checkArgument(length == v.size)
        var j = 0

        for (i in offset until (offset + length)) {
            data[i] = v[j++]
        }
    }

    override fun plusAssign(v: Vector) {
        var j = 0
        for (i in offset until offset + length) {
            data[i] = data[i] + v[j++]
        }
    }

    override fun plusAssign(v: Pair<Number, Vector>) {
        val scale = v.first.toFloat()
        val vector = v.second
        if (scale == 1.0f) {
            plusAssign(v)
        }else{
            var j = 0
            for (i in offset until offset + length) {
                data[i] = data[i] + vector[j++] * scale
            }
        }
    }

    override fun minusAssign(x: Vector) {
        var j = 0
        for (i in offset until offset + length) {
            data[i] = data[i] - x[j++]
        }
    }

    override fun minusAssign(x: Pair<Number, Vector>) {
        val scale = x.first.toFloat()
        val vector = x.second
        var j = 0
        for (i in offset until offset + length) {
            data[i] = data[i] - vector[j++] * scale
        }
    }

    override fun timesAssign(scale: Number) {
        val scale = scale.toFloat()
        for (i in offset until offset + length) {
            data[i] *= scale
        }
    }

    override fun divAssign(scale: Number) {
        val scale = scale.toFloat()
        for (i in offset until offset + length) {
            data[i] /= scale
        }
    }
}


/**
 * 行存储的只读矩阵。内存实现
 */
class FloatArrayMatrix(rows: Int, cols: Int, val data: FloatArray) : BaseMatrix(rows, cols), FloatMatrix {

    override fun write(channel: FileChannel) {
        val byteBuffer = ByteBuffer.allocate(cols * 4)
        val asFloatBuffer = byteBuffer.asFloatBuffer()
        for (row in 0 until rows) {
            asFloatBuffer.clear()
            asFloatBuffer.put(data, row * cols, cols)

            byteBuffer.position(0)
            byteBuffer.limit(cols*4)
            channel.write(byteBuffer)
        }
    }

    //private var length = rows * cols

    private val rowView = (0 until rows).mapIndexed { index, _ -> FloatArrayVector(data, index * cols, cols) }.toTypedArray()

    /**
     * 行视图
     */
    override operator fun get(row: Int): Vector {
        return rowView[row]
    }

    override operator fun get(i: Int, j: Int): Float {
        return data[i * cols + j]
    }
}


/**
 * 数据全部加载到内存的.
 * 按行存储的内存矩阵
 */
class MutableFloatArrayMatrix(rows: Int, cols: Int) : BaseMatrix(rows, cols), MutableFloatMatrix {

    override fun write(channel: FileChannel) {
        val byteBuffer = ByteBuffer.allocateDirect(cols * 4)
        val asFloatBuffer = byteBuffer.asFloatBuffer()

        byteBuffer.position(0)
        byteBuffer.limit(cols)

        for (row in 0 until rows) {
            asFloatBuffer.clear()
            asFloatBuffer.put(data, row * cols, cols)
            channel.write(byteBuffer)
        }
    }

    private var length = rows * cols

    var data = FloatArray(length)

    private val rnd: Random = Random()

    private val rowview = (0 until rows).mapIndexed { index, i -> MutableFloatArrayVector(data, index * cols, cols) }.toTypedArray()

    constructor() : this(0, 0)

    override fun uniform(a: Number) {
        var a = a.toFloat()
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
    override operator fun get(row: Int): MutableVector {
        return rowview[row]
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