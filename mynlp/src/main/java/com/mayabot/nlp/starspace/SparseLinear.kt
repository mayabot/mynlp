package com.mayabot.nlp.starspace

import com.mayabot.nlp.blas.DenseMatrix
import com.mayabot.nlp.blas.DenseVector
import com.mayabot.nlp.blas.Vector


open class TheMatrix(val matrix: DenseMatrix) {

    fun numRows(): Int {
        return matrix.row
    }

    fun numCols(): Int {
        return matrix.col
    }

}


class SparseLinear(matrix: DenseMatrix) : TheMatrix(matrix) {

    fun forward(row: Int): Vector {
        return matrix[row]
    }

    fun forward(list: List<XPair>): Vector {

        val vector = DenseVector(this.numCols())

        for ((row, scale) in list) {
            vector += scale to matrix[row]
        }

        return vector
    }

}