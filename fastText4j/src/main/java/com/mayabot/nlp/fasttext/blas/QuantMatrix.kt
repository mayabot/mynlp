package com.mayabot.nlp.fasttext.blas

import com.mayabot.nlp.fasttext.utils.*
import java.io.File
import java.nio.channels.FileChannel

fun buildQMatrix(mat: DenseMatrix,
                 dsub: Int = 2,
                 qnorm: Boolean = false): QuantMatrix {

    val m = mat.row
    val n = mat.col

    val codeSize = m * pages(n, dsub)

    val pq = ProductQuantizer(n, dsub)
    val codes = ShortArray(codeSize)

    val npq = ProductQuantizer(1, 1)
    val normCodes = if (qnorm) {
        ShortArray(m)
    } else {
        ShortArray(0)
    }

    val qm = QuantMatrix(mat.row, mat.col, codeSize, pq, npq, codes, normCodes, qnorm)

    qm.quantize(mat)

    return qm
}

fun loadQuantMatrix(file: File): QuantMatrix {
    return file.openDataInputStream().use {
        loadQuantMatrix(AutoDataInput(it))
    }
}

fun loadQuantMatrix(input: AutoDataInput): QuantMatrix {
    val qnorm = input.readUnsignedByte() != 0
    val m = input.readLong().toInt()
    val n = input.readLong().toInt()
    val codeSize = input.readInt()

    val codes = ShortArray(codeSize)
    input.readShortArray(codes)

    val pq = ProductQuantizer.loadFromBuffer(input)

    val (npq, normCodes) = if (qnorm) {
        val normCodes = ShortArray(m)

        input.readShortArray(normCodes)

        val npq = ProductQuantizer.loadFromBuffer(input)

        npq to normCodes
    } else {
        ProductQuantizer(1, 1) to ShortArray(0)
    }

    return QuantMatrix(m, n, codeSize, pq, npq, codes, normCodes, qnorm)
}

//fun loadQuantMatrix(buffer: AutoDataInput): QMatrix {
//    val qnorm_ = buffer.readUnsignedByte().toInt() != 0
//    val m = buffer.readLong().toInt()
//    val n = buffer.readLong().toInt()
//    val codeSize = buffer.readInt()
//
//    val codes = ShortArray(codeSize)
//    buffer.readShortArray(codes)
//
//    val qmatrix = QMatrix(m = m, n = n, dsub = 2, qnorm = qnorm_)
//    qmatrix.codesize_ = codeSize
//    qmatrix.codes_ = codes
//
//    val pq_ = ProductQuantizer.loadFromBuffer(buffer)
//
//    qmatrix.pq_ = pq_
//    qmatrix.qnorm = qnorm_
//
//    if (qnorm_) {
//        val normCodes = ShortArray(m)
//        buffer.readShortArray(normCodes)
//        qmatrix.norm_codes_ = normCodes
//
//
//        val npq = ProductQuantizer.loadFromBuffer(buffer)
//        qmatrix.npq_ = npq
//    }
//
//    return qmatrix
//}

class QuantMatrix(val m: Int,
                  val n: Int,
                  val codeSize: Int,
                  val pq: ProductQuantizer,
                  val npq: ProductQuantizer,
                  val codes: ShortArray,
                  val normCodes: ShortArray,
                  val qnorm: Boolean = false) : Matrix {

    override val row = m
    override val col = n

    fun quantize(matrix: DenseMatrix) {

        if (qnorm) {
            val norms = floatArrayVector(m)
            matrix.l2NormRow(norms)
            matrix.divideRow(norms)
            quantizeNorm(norms)
        }

        pq.train(matrix)
        loggerln("compute_codes...")
        pq.compute_codes(matrix, codes)
        loggerln("compute_codes success")
    }

    private fun quantizeNorm(norms: DenseVector) {
        assert(qnorm)
        assert(norms.length() == m)

        val data = floatArrayMatrix(m, 1, norms.data())
        npq.train(data)
        npq.compute_codes(data, normCodes)
    }

    /**
     * 矩阵的第i行和vec进行点积计算
     */
    override fun dotRow(vec: Vector, i: Int): Float {
        check(i in 0 until m && vec.length() == n)

        var norm = 1f
        if (qnorm) {
            norm = npq.centroidTable.centroidData[npq.get_centroids(0, normCodes[i])]
        }

        return pq.mulCode(vec, codes, i, norm)
    }


    override fun addVectorToRow(vector: Vector, rows: Int, a: Float) {
        error("Operation not permitted on quantized")
    }

    override fun addRowToVector(target: Vector, i: Int, a: Double?) {
        var norm = 1
        if (qnorm) {
            norm = npq.get_centroids(0, normCodes[i])
        }
        if (a == null) {
            pq.addCode(target, codes, i, norm.toFloat())
        } else {
            pq.addCode(target, codes, i, (a * norm).toFloat())
        }
    }

    override fun save(file: File) {
        file.outputStream().channel.use { channel ->
            save(channel)
        }
    }

    override fun save(channel: FileChannel) {
        with(channel) {
            writeBoolean(qnorm)
            writeLong(m.toLong())
            writeLong(n.toLong())
            writeInt(codeSize)
            writeShortArray(codes)

            pq.save(channel)
            if (qnorm) {
                writeShortArray(normCodes)
                npq.save(channel)
            }
        }
    }
}
