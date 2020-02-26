package com.mayabot.nlp.fasttext.blas

import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * FloatArray的向量
 */
class DenseVector(
        /**
         * 底层的Float Array
         */
        private val data: FloatArray,
        private val offset: Int,
        private val length: Int) : Vector {


    /**
     * 把[data]当做向量
     */
    constructor(data: FloatArray) : this(data, 0, data.size)

    /**
     * 构建指定[size]长度的Vector，初始值为空
     */
    constructor(size: Int) : this(FloatArray(size), 0, size)

    constructor(size: Int, initValue: Float) : this(FloatArray(size) { initValue }, 0, size)

    companion object {
        private const val serialVersionUID: Long = 112L
    }

    fun data() = data

    override fun subVector(offset: Int, size: Int): Vector {
        val result = FloatArray(size)
        for (i in 0 until size) {
            result[i] += this[i + offset]
        }
        return DenseVector(result, 0, size)
    }

    override fun plusTo(v: Vector): Vector {
        check(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] + v[i]
        }
        return DenseVector(result, 0, length)
    }

    override fun minusTo(v: Vector): Vector {
        check(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] - v[i]
        }
        return DenseVector(result, 0, length)
    }

    /**
     * index 0 until length
     */
    override fun get(index: Int): Float {
        return data[index + offset]
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
        var norm = 0.0f

        for (i in offset until (offset + length)) {
            val x = data[i]
            norm += x * x
        }

        if (norm.isNaN()) {
            throw Exception()
        }

        return sqrt(norm)
    }

    override fun norm2Pow(): Float {
        var sum = 0.0f
        for (i in offset until (offset + length)) {
            val x = data[i]
            sum += x * x
        }
        return sum
    }

    override fun check() {
        for (i in offset until offset + length) {
            val f = data[i]
            check(!f.isNaN())
            check(!f.isInfinite())
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

    override fun copy(): Vector {
        val dest = FloatArray(length)
        System.arraycopy(this.data, offset, dest, 0, length)
        return DenseVector(dest, 0, length)
    }

    override fun fill(v: Number) {
        val value = v.toFloat()
        for (i in offset until offset + length) {
            data[i] = value
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
        check(this.length() == v.length())

        var j = offset
        for (i in 0 until v.length()) {
            data[j++] = v[i]
        }
    }

    override fun set(index: Int, value: Float) {
        data[index + offset] = value
    }

    override fun putAll(v: FloatArray) {
        check(length == v.size)
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
            plusAssign(v.second)
        } else {
            var j = 0
            for (i in offset until offset + length) {
                data[i] = data[i] + vector[j++] * scale
            }
        }
    }

    override fun minusAssign(vector: Vector) {
        var j = 0
        for (i in offset until offset + length) {
            data[i] = data[i] - vector[j++]
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
        val scaleFloat = scale.toFloat()
        for (i in offset until offset + length) {
            data[i] *= scaleFloat
        }
    }

    override fun divAssign(scale: Number) {
        val scaleFloat = scale.toFloat()
        for (i in offset until offset + length) {
            data[i] /= scaleFloat
        }
    }
}


/**
 * 基于ByteBuffer存储的Vector实现
 * @author jimichan
 */
class ByteBufferDenseVector(private val data: ByteBuffer,
                            private val offset: Int,
                            private val length: Int) : Vector {

    companion object {
        private const val serialVersionUID: Long = 1234234218L
    }

    constructor(size: Int) : this(ByteBuffer.allocate(size shl 2), 0, size)

    constructor(data: ByteBuffer) : this(data, 0, data.capacity() / 4)

    override fun plusTo(v: Vector): Vector {
        check(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] + v[i]
        }
        return DenseVector(result, 0, length)
    }

    override fun minusTo(v: Vector): Vector {
        check(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] - v[i]
        }
        return DenseVector(result, 0, length)
    }

    override fun subVector(offset: Int, size: Int): Vector {
        val result = FloatArray(size)
        for (i in 0 until size) {
            result[i] += this[i + offset]
        }
        return DenseVector(result, 0, size)
    }


    /**
     * index 0 until length
     */
    override fun get(index: Int): Float {
        return data.getFloat((index + offset) shl 2)
    }

    override fun length(): Int = length

    override fun times(vector: Vector) = this.prod(vector)

    override fun prod(vector: Vector): Float {
        var result = 0f
        var j = 0
        for (i in offset until offset + length) {
            result += data.getFloat(i shl 2) * vector[j++]
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
        return sqrt(sum.toDouble()).toFloat()
    }

    override fun norm2Pow(): Float {
        var sum = 0.0f
        for (i in offset shl 2 until (offset + length) * 4 step 4) {
            val x = data.getFloat(i)
            sum += x * x
        }
        return sum
    }

    override fun check() {
        for (i in offset until offset + length) {
            val f = data.getFloat(i shl 2)
            check(!f.isNaN())
            check(!f.isInfinite())
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

    override fun copy(): Vector {
        val dest = ByteBuffer.allocate(length shl 2)
        for (i in offset shl 2 until (offset + length) * 4) {
            dest.put(data.get(i))
        }
        return ByteBufferDenseVector(dest, 0, length)
    }

    override fun fill(value: Number) {
        val v = value.toFloat()
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

    override fun invoke(vector: Vector) {
        check(this.length() == vector.length())
        for (i in 0 until vector.length()) {
            this[i] = vector[i]
        }
    }

    override fun set(index: Int, value: Float) {
        data.putFloat((index + offset) shl 2, value)
    }

    override fun putAll(v: FloatArray) {
        check(length == v.size)
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

    override fun minusAssign(pair: Pair<Number, Vector>) {
        val scale = pair.first.toFloat()
        val vector = pair.second
        var j = 0
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) - vector[j++] * scale)
        }
    }

    override fun timesAssign(scale: Number) {
        val scaleFloat = scale.toFloat()
        for (i in (offset * 4 until (offset + length) * 4 step 4)) {
            data.putFloat(i, data.getFloat(i) * scaleFloat)
        }
    }

    override fun divAssign(scale: Number) {
        val scaleFloat = scale.toFloat()
        if (scaleFloat != 0f) {
            for (i in (offset * 4 until (offset + length) * 4 step 4)) {
                data.putFloat(i, data.getFloat(i) / scaleFloat)
            }
        }
    }
}
