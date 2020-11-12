package com.mayabot.nlp.algorithm

/**
 * 默认小顶堆。如果需要大顶堆
 *
 *
 */
class TopHeap<T>(
        val maxSize: Int,
        val comparator: Comparator<T>,
        /**
         * false 表示大顶堆
         */
        val minTop: Boolean = true
) {

    private val data = arrayOfNulls<Any>(maxSize)

    private var size: Int = 0

    fun push(data: T) {

    }

    private fun heapify() {

    }

    fun root(): T {
        TODO()
    }

    /**
     * 获取里面的所有元素，但是并不是排好序的
     */
    fun toList(): List<T> {
        TODO()
    }

}