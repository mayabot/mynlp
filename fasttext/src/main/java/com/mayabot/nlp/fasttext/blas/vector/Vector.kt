package com.mayabot.nlp.fasttext.blas.vector

import java.io.Serializable
import java.nio.ByteBuffer
import kotlin.math.sqrt



/**
 * Float向量
 * @author jimichan
 */
interface Vector : Serializable {

    fun subVector(offset: Int, size: Int): Vector

    fun plusTo(v: Vector): Vector

    fun minusTo(v: Vector): Vector

    fun divTo(float: Float): Vector {
        val result = FloatArray(length())
        for (i in 0 until length()) {
            result[i] = this[i] / float
        }
        return FloatArrayVector(result, 0, length())
    }


    operator fun get(index: Int): Float

    fun length(): Int

    fun prod(v: Vector): Float

    operator fun times(v: Vector): Float

    fun norm2(): Float

    fun norm2Pow(): Float

    fun check()

    fun copy(): Vector

    fun access(call: (Int, Float) -> Unit)

    // writer
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

fun byteBufferVector(size: Int) = ByteBufferVector(ByteBuffer.allocate(size shl 2), 0, size)
fun directByteBufferVector(size: Int) = ByteBufferVector(ByteBuffer.allocateDirect(size shl 2), 0, size)
fun floatArrayVector(size: Int) = FloatArrayVector(size)

/**
 * 向量点积
 */
fun dot(a: Vector, b: Vector): Float {
    return a * b
}

/**
 * 向量余弦
 */
fun cosine(a: Vector, b: Vector): Float {
    val normA = a * a
    val normB = b * b
    return if (normA == 0.0f || normB == 0.0f) {
        0.0f
    } else {
        (a * b / sqrt((normA * normB).toDouble())).toFloat()
    }
}

operator fun ByteBuffer.set(i: Int, v: Float) {
    this.putFloat(i shl 2, v)
}


