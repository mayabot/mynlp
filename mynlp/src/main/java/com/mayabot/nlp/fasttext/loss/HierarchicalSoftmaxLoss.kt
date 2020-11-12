package com.mayabot.nlp.fasttext.loss

import com.mayabot.nlp.blas.Matrix
import com.mayabot.nlp.blas.Vector
import com.mayabot.nlp.common.IntArrayList
import com.mayabot.nlp.fasttext.Model
import com.mayabot.nlp.fasttext.Predictions
import com.mayabot.nlp.fasttext.ScoreIdPair
import java.util.*
import kotlin.math.exp

class HierarchicalSoftmaxLoss(wo: Matrix, targetCounts: LongArray) : BinaryLogisticLoss(wo) {

    val osz = targetCounts.size

    val paths: MutableList<IntArray>
    val codes: MutableList<BooleanArray>
    val tree: MutableList<Node>

    // build three logic
    init {

        val counts = targetCounts

        val osz = wo.row

        val pathsLocal = ArrayList<IntArray>(osz)
        val codesLocal = ArrayList<BooleanArray>(osz)
        val treeLocal = ArrayList<Node>(2 * osz - 1)

        for (i in 0 until 2 * osz - 1) {
            treeLocal.add(Node().apply {
                this.parent = -1
                this.left = -1
                this.right = -1
                this.count = 1000000000000000L// 1e15f;
                this.binary = false
            })
        }

        for (i in 0 until osz) {
            treeLocal[i].count = counts[i]
        }

        var leaf = osz - 1
        var node = osz
        for (i in osz until 2 * osz - 1) {
            val mini = IntArray(2)
            for (j in 0..1) {
                if (leaf >= 0 && treeLocal[leaf].count < treeLocal[node].count) {
                    mini[j] = leaf--
                } else {
                    mini[j] = node++
                }
            }
            treeLocal[i].apply {
                this.left = mini[0]
                this.right = mini[1]
                this.count = treeLocal[mini[0]].count + treeLocal[mini[1]].count
            }
            treeLocal[mini[0]].parent = i
            treeLocal[mini[1]].parent = i
            treeLocal[mini[1]].binary = true
        }

        for (i in 0 until osz) {
            val path = ArrayList<Int>()
            val code = ArrayList<Boolean>()

            var j = i
            while (treeLocal[j].parent != -1) {
                path.add(treeLocal[j].parent - osz)
                code.add(treeLocal[j].binary)
                j = treeLocal[j].parent
            }
            pathsLocal.add(path.toIntArray())
            codesLocal.add(code.toBooleanArray())
        }

        this.paths = pathsLocal
        this.codes = codesLocal
        this.tree = treeLocal
    }

    private fun dfs(k: Int, threshold: Float, node: Int, score: Float, heap: MutableList<ScoreIdPair>, hidden: Vector) {

        if (score < stdLog(threshold)) {
            return
        }

        if (heap.size == k && score < heap[heap.size - 1].score) {
            return
        }

        if (tree[node].left == -1 && tree[node].right == -1) {
            heap.add(ScoreIdPair(score, node))
            //Collections.sort(heap, comparePairs)
            heap.sortByDescending { it.score }
            if (heap.size > k) {
                //Collections.sort(heap, comparePairs)
                heap.sortByDescending { it.score }
                heap.removeAt(heap.size - 1) // pop last
            }
            return
        }

        var f = wo.dotRow(hidden, node - osz)
        f = 1.0f / (1 + exp(-f))
////        val f = sigmoid(output.dotRow(hidden, node - outputMatrixSize))
//        var f = if (quant && quantOut) {
//            qoutput.dotRow(hidden, node - outputMatrixSize)
//        } else {
//            output[node - outputMatrixSize] * hidden
//        }
//        f = 1.0f / (1 + exp(-f))


        dfs(k, threshold, tree[node].left, score + stdLog(1.0f - f).toFloat(), heap, hidden)
        dfs(k, threshold, tree[node].right, score + stdLog(f).toFloat(), heap, hidden)
    }

    override fun forward(targets: IntArrayList, targetIndex: Int, state: Model.State, lr: Float, backprop: Boolean): Float {
        var loss = 0f
        val target = targets[targetIndex]
        val binaryCode = codes[target]
        val pathToRoot = paths[target]
        for (i in pathToRoot.indices) {
            loss += binaryLogistic(pathToRoot[i], state, binaryCode[i], lr, backprop)
        }
        return loss
    }


    override fun predict(k: Int, threshold: Float, heap: Predictions, state: Model.State) {
        dfs(k, threshold, 2 * osz - 2, 0f, heap, state.hidden)
        heap.sortByDescending { it.score }
    }

    class Node {
        @JvmField
        var parent: Int = 0
        @JvmField
        var left: Int = 0
        @JvmField
        var right: Int = 0
        @JvmField
        var count: Long = 0
        @JvmField
        var binary: Boolean = false
    }

}