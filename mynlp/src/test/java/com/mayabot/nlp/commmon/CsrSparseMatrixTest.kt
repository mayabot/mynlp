package com.mayabot.nlp.commmon

import com.mayabot.nlp.common.TreeBasedTable
import com.mayabot.nlp.common.matrix.CSRSparseMatrix
import org.junit.Assert
import org.junit.Test

class CsrSparseMatrixTest {

    @Test
    fun test() {
        val table: TreeBasedTable<Int, Int, Int> = TreeBasedTable()

        table.put(2, 0, 6)
        table.put(3, 2, 4)
        table.put(0, 0, 5)
        table.put(0, 3, 2)
        table.put(4, 1, 2)
        table.put(4, 4, 9)

        val csr = CSRSparseMatrix(table, 5)

        Assert.assertTrue(csr[2, 0] == 6)
        Assert.assertTrue(csr[3, 2] == 4)
        Assert.assertTrue(csr[0, 0] == 5)
        Assert.assertTrue(csr[0, 3] == 2)
        Assert.assertTrue(csr[4, 1] == 2)
        Assert.assertTrue(csr[4, 4] == 9)
    }
}