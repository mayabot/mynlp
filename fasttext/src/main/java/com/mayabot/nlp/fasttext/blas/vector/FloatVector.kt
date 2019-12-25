package com.mayabot.nlp.fasttext.blas.vector

import com.google.common.base.Preconditions


/**
 * FloatArray的向量
 */
class FloatArrayVector(
        @JvmField protected val data: FloatArray,
        @JvmField protected val offset: Int,
        @JvmField protected val length: Int) : Vector {

    constructor(data: FloatArray) : this(data, 0, data.size)

    constructor(size: Int) : this(FloatArray(size), 0, size)

    companion object {
        private const val serialVersionUID: Long = 112L
    }

    override fun subVector(offset: Int, size: Int): Vector {
        val result = FloatArray(size)
        for (i in 0 until size) {
            result[i] += this[i + offset]
        }
        return FloatArrayVector(result, 0, size)
    }

    override fun plusTo(v: Vector): Vector {
        Preconditions.checkArgument(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] + v[i]
        }
        return FloatArrayVector(result, 0, length)
    }

    override fun minusTo(v: Vector): Vector {
        Preconditions.checkArgument(length == v.length())
        val result = FloatArray(length)
        for (i in 0 until length) {
            result[i] = this[i] - v[i]
        }
        return FloatArrayVector(result, 0, length)
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
        var sum = 0.0f
        for (i in offset until (offset + length)) {
            val x = data[i]
            sum += x * x
        }
        return Math.sqrt(sum.toDouble()).toFloat()
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
            Preconditions.checkArgument(!f.isNaN())
            Preconditions.checkArgument(!f.isInfinite())
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
        return FloatArrayVector(dest, 0, length)
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
        Preconditions.checkArgument(this.length() == v.length())

        var j = offset
        for (i in 0 until v.length()) {
            data[j++] = v[i]
        }
    }

    override fun set(index: Int, value: Float) {
        data[index + offset] = value
    }

    override fun putAll(v: FloatArray) {
        Preconditions.checkArgument(length == v.size)
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
