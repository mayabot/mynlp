package com.mayabot.nlp.fasttext.blas

import java.nio.ByteBuffer
import kotlin.math.sqrt

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

fun floatArrayVector(size: Int) = DenseVector(size)
fun byteBufferVector(size: Int) = ByteBufferDenseVector(ByteBuffer.allocate(size shl 2), 0, size)
fun directByteBufferVector(size: Int) = ByteBufferDenseVector(ByteBuffer.allocateDirect(size shl 2), 0, size)

fun floatArrayMatrix(rows: Int, cols: Int, data: FloatArray) = DenseArrayMatrix(rows, cols, data)
fun floatArrayMatrix(rows: Int, cols: Int) = DenseArrayMatrix(rows, cols)
fun byteBufferMatrix(rows: Int, cols: Int) = ByteBufferMatrix(rows, cols, false)
fun directByteBufferMatrix(rows: Int, cols: Int) = ByteBufferMatrix(rows, cols, true)
