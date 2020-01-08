package com.mayabot.nlp.fasttext.utils

import java.util.*
import kotlin.math.min

/**
 * 求最大Top K
 * 内部是小顶堆
 *
 * @author jimichan
 */
class TopMaxK<T>(private val k: Int=10 ) {

    private val heap: FloatArray = FloatArray(k)
    private val idIndex: MutableList<T?> = MutableList(k){null}
    var size = 0

    fun push(id: T, score: Float) {
        if (size < k) {
            heap[size] = score
            idIndex[size] = id
            size++
            if (size == k) {
                buildMinHeap()
            }
        } else { // 如果这个数据大于最下值，那么有资格进入
            if (score > heap[0]) {
                heap[0] = score
                idIndex[0] = id
                mintopify(0)
            }
        }
    }

    fun canPush(score: Float): Boolean {
        if (size < k) {
           return true
        } else { // 如果这个数据大于最下值，那么有资格进入
            if (score > heap[0]) {
                return true
            }
        }
        return false
    }

    fun result(): ArrayList<Pair<T, Float>> {
        val top = min(k, size)
        val list = ArrayList<Pair<T, Float>>(top)
        for (i in 0 until top) {
            val v = idIndex[i]
            val s = heap[i]
            if(v!=null){
                list += v to s
            }
        }
        list.sortByDescending { it.second }
        return list
    }

    private fun buildMinHeap() {
        for (i in k / 2 - 1 downTo 0) { // 依次向上将当前子树最大堆化
            mintopify(i)
        }
    }

    /**
     * 让heap数组符合堆特性
     *
     * @param i
     */
    private fun mintopify(i: Int) {
        val l = 2 * i + 1
        val r = 2 * i + 2
        var min = 0
        min = if (l < k && heap[l] < heap[i]) {
            l
        } else {
            i
        }
        if (r < k && heap[r] < heap[min]) {
            min = r
        }
        if (min == i || min >= k) {
            // 如果largest等于i说明i是最大元素
            // largest超出heap范围说明不存在比i节点大的子女
            return
        }
        swap(i, min)
        mintopify(min)
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