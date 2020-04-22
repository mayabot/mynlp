package com.mayabot.nlp.fasttext.blas

import java.io.File
import java.io.Serializable
import java.nio.channels.FileChannel

/**
 * Float矩阵
 */
interface Matrix : Serializable {

    val row: Int
    val col: Int

    /**
     * 矩阵的第i行和vec进行点积计算
     */
    fun dotRow(vec: Vector, i: Int): Float

    /**
     * 把[vector]加到指定的[row] , [a]是系数
     */
    fun addVectorToRow(vector: Vector, row: Int, a: Float)

    fun addRowToVector(target: Vector, i: Int, a: Double? = null)

    fun save(file: File)

    fun save(channel: FileChannel)


}

interface DenseMatrix : Matrix {

    fun zero()
    //    fun fill(v: Float)
    fun uniform(number: Number)

    operator fun get(row: Int): Vector
    operator fun get(i: Int, j: Int): Float

    operator fun set(i: Int, j: Int, v: Float)

    /**
     * 乘法
     *
     * 从ib到ie这些行，系数存在vector里面
     */
    fun multiplyRow(nums: Vector, ib: Int = 0, ie: Int = -1)

    /**
     * 除法
     */
    fun divideRow(nums: Vector, ib: Int = 0, ie: Int = -1)

    fun l2NormRow(i: Int): Float
    fun l2NormRow(norms: Vector)

}