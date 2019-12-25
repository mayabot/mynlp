package com.mayabot.nlp.fasttext.blas.vector

import java.nio.ByteBuffer


/**
 * 基于ByteBuffer存储的Vector实现
 * @author jimichan
 */
class ByteBufferVector(@JvmField private val data: ByteBuffer,
                       @JvmField private val offset: Int,
                       @JvmField private val length: Int) : Vector {

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
        return FloatArrayVector(result, 0, length)
    }

    override fun minusTo(v: Vector): Vector {
        check(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] - v[i]
        }
        return FloatArrayVector(result, 0, length)
    }

    override fun subVector(offset: Int, size: Int): Vector {
        val result = FloatArray(size)
        for (i in 0 until size) {
            result[i] += this[i + offset]
        }
        return FloatArrayVector(result, 0, size)
    }


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
        return ByteBufferVector(dest, 0, length)
    }

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
        check(this.length() == v.length())
        for (i in 0 until v.length()) {
            this[i] = v[i]
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
