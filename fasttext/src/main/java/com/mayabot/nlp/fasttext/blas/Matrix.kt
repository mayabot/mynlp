package com.mayabot.nlp.fasttext.blas

import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.readInt
import java.io.File
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.min

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

    fun addRowToVector(target: Vector,i: Int,a: Double?=null)

    fun save(file: File)

    companion object {

        fun loadMatrix(file: File, mmap: Boolean): Matrix {
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
    }
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
    fun multiplyRow(nums:Vector, ib:Int=0,ie:Int = -1)

    /**
     * 除法
     */
    fun divideRow(nums:Vector, ib:Int=0,ie:Int = -1)

    fun l2NormRow(i: Int) : Float
    fun l2NormRow(norms: Vector)

}
//
//abstract class BaseMatrix(override val row: Int,
//                          override val col: Int) : Matrix {
//
//    override fun toString(): String {
//        if (row == 0) {
//            return ""
//        }
//
//        val b = StringBuilder()
//
//        b.append("-".repeat(col * 12))
//        b.append("\n")
//
//        for (i in 0 until min(20, row)) {
//            val row = get(i)
//
//            for (j in 0 until col) {
//                b.append(row[j]).append("\t")
//            }
//
//            b.append("\n")
//        }
//
//        if (row > 20) {
//            b.append("....more....")
//        }
//        b.append("\n")
//
//        return b.toString()
//    }
//}

