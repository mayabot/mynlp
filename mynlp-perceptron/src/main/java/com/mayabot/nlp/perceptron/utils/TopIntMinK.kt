package com.mayabot.nlp.perceptron.utils

/**
 * Top K 最小值。
 */
class TopIntMinK(private val k: Int) {

    private val heap = FloatArray(k)
    private val idIndex = IntArray(k) { -1 }

    var size = 0

    fun push(id: Int, score: Float) {
        if (size < k) {
            heap[size] = score
            idIndex[size] = id
            size++

            if (size == k) {
                buildMinHeap()
            }
        } else {
            // 如果这个数据小于最大值，那么有资格进入
            if (score < heap[0]) {
                heap[0] = score
                idIndex[0] = id

                topify(0)
            }
        }
    }

    fun result(): ArrayList<Pair<Int, Float>> {
        val top = Math.min(k, size)
        val list = ArrayList<Pair<Int, Float>>(top)

        for (i in 0 until top) {
            list += idIndex[i] to heap[i]
        }

        list.sortBy { it.second }
        return list
    }

    private fun buildMinHeap() {
        for (i in k / 2 - 1 downTo 0) {
            topify(i)// 依次向上将当前子树最大堆化
        }
    }

    private fun topify(i: Int) {
        val l = 2 * i + 1
        val r = 2 * i + 2
        var max: Int

        if (l < k && heap[l] > heap[i])
            max = l
        else
            max = i

        if (r < k && heap[r] > heap[max]) {
            max = r
        }

        if (max == i || max >= k)
        // 如果largest等于i说明i是最大元素
        // largest超出heap范围说明不存在比i节点大的子女
            return

        swap(i, max)
        topify(max)
    }

    private fun swap(i: Int, j: Int) {
        val tmp = heap[i]
        heap[i] = heap[j]
        heap[j] = tmp

        val tmp2 = idIndex[i]
        idIndex[i] = idIndex[j]
        idIndex[j] = tmp2
    }
}