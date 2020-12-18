package com.mayabot.nlp.starspace

import java.io.File
import java.nio.file.Files
import java.util.*

fun String.toFile() = File(this)

fun File.lines() = Files.lines(this.toPath())!!

fun File.forEachLine(action: (String) -> Unit) = Files.lines(this.toPath()).forEach(action)

fun File.firstLine(): String? = Files.lines(this.toPath()).findFirst().orElse(null)


inline fun checkArgument(expression: Boolean) {
    if (!expression) {
        throw IllegalArgumentException()
    }
}

object NormalizeText {
    /*
     * We categorize longer strings into the following buckets:
     *
     * 1. All punctuation-and-numeric. Things in this bucket get
     *    their numbers flattened, to prevent combinatorial explosions.
     *    They might be specific numbers, prices, etc.
     *
     * 2. All letters: case-flattened.
     *
     * 3. Mixed letters and numbers: a product ID? Flatten case and leave
     *    numbers alone.
     *
     * The case-normalization is state-machine-driven.
     */

    @JvmStatic
    fun normalize(str: String): String {

        var allNumeric = true
        var containsDigits = false

        for (i in 0 until str.length) {
            val c = str[i]

            containsDigits = containsDigits or Character.isDigit(c)

            if (!Character.isAlphabetic(c.toInt())) {
                allNumeric = false
                continue
            }
            if (!Character.isAlphabetic(c.toInt())) continue
            allNumeric = false
        }

        val flattenCase = true
        val flattenNum = allNumeric && containsDigits
        if (!flattenNum && !flattenCase) return str

        val chars = str.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (flattenNum && Character.isDigit(c)) {
                chars[i] = '0'
            }
            if (Character.isAlphabetic(c.toInt())) {
                chars[i] = Character.toLowerCase(c)
            }
        }
        return String(chars)
    }
}


/**
 * Top K 最小值。
 */
class TopMinK(val k: Int) {

    val heap = FloatArray(k)
    val idIndex = IntArray(k) { -1 }

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

    fun topify(i: Int) {
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

/**
 * 求最大Top K
 * 内部是小顶堆
 */
class TopMaxK(val k: Int) {

    val heap = FloatArray(k)
    val idIndex = IntArray(k) { -1 }

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
            // 如果这个数据大于最下值，那么有资格进入
            if (score > heap[0]) {
                heap[0] = score
                idIndex[0] = id

                mintopify(0)
            }
        }
    }

    fun resort(): ArrayList<Pair<Int, Float>> {
        val top = Math.min(k, size)
        val list = ArrayList<Pair<Int, Float>>(top)

        heap.forEachIndexed { index, fl ->
            if (size < k && index < size) {
                list += idIndex[index] to fl
            } else {
                list += idIndex[index] to fl
            }

        }
        list.sortByDescending { it.second }
        return list
    }

    private fun buildMinHeap() {
        for (i in k / 2 - 1 downTo 0) {
            mintopify(i)// 依次向上将当前子树最大堆化
        }
    }

    /**
     * 让heap数组符合堆特性
     * @param i
     */
    private fun mintopify(i: Int) {
        val l = 2 * i + 1
        val r = 2 * i + 2
        var min: Int

        if (l < k && heap[l] < heap[i])
            min = l
        else
            min = i

        if (r < k && heap[r] < heap[min])
            min = r

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