package com.mayabot.nlp.blas

import java.io.Serializable


/**
 * Float Vector
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
        return DenseVector(result, 0, length())
    }

    fun length(): Int

    fun prod(vector: Vector): Float

    fun norm2(): Float

    fun norm2Pow(): Float

    fun check()

    fun copy(): Vector

    fun access(call: (Int, Float) -> Unit)

    // writer
    fun fill(value: Number)

    fun fill(call: (Int) -> Float)

    operator fun get(index: Int): Float

    operator fun times(vector: Vector): Float

    operator fun set(index: Int, value: Float)

    operator fun plusAssign(vector: Vector)

    operator fun plusAssign(pair: Pair<Number, Vector>)

    operator fun minusAssign(vector: Vector)

    operator fun minusAssign(pair: Pair<Number, Vector>)

    operator fun timesAssign(scale: Number)

    operator fun divAssign(scale: Number)

    fun putAll(array: FloatArray)

    fun zero()


    /**
     * 赋值
     */
    operator fun invoke(vector: Vector)

    /**
     *
     */
    fun addRow(A: Matrix, i: Int, a: Double) {
        check(i >= 0)
        check(i < A.row)
        check(length() == A.col)
        A.addRowToVector(this, i, a)
    }

    /**
     * 矩阵的每一行和vec点积，保存在当前这个向量里面
     */
    fun mul(A: Matrix, vec: Vector) {
        check(A.row == length())
        check(A.col == vec.length())
        for (i in 0 until length()) {
            this[i] = A.dotRow(vec, i)
        }
    }

}