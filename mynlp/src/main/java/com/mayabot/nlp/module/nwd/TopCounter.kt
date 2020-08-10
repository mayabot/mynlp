package com.mayabot.nlp.module.nwd

/**
 * @author jimichan
 * 统计元素重复最高
 */
class TopCounter(private val size: Int = 2000000,
                 private var minCount: Int = 10) {

    private var verbose = false

    var data = HashMap<String, IntCount>(size)

    private var topList = listOf<String>()

    private var lastMinCount = 2

    fun put(key: String) {

        val v = data[key]
        if (v == null) {
            data[key] = IntCount()
        } else {
            v.value++
        }

        if (data.size >= size) {
            reduce()
        }
    }

    private fun reduce() {
        //1. remove count less min
        if (verbose) println("清洗前有${data.size}条数据")

        val target = size / 4 //压缩为1/4

        var max = 0

        for (min in lastMinCount until minCount) {
            if (data.size > target) {
                //data.removeAll { _, value -> value <= min }
                data = data.filterTo(HashMap()) { it.value.value > min }
                if (verbose) println("删除小于 ${min} 的数量,剩余${data.size}")
                max = min
            }
        }

        lastMinCount = max - 1
        if (lastMinCount <= 2) {
            lastMinCount = 2
        }

        //还超出一半
        if (data.size > size / 2) {
            minCount++
        }

        if (verbose) println("-".repeat(20))
    }

    fun clean() {
        data = data.filterTo(HashMap()) { it.value.value > minCount }
    }

    fun getListResult(): List<WordCount> {
        clean()
        val list = ArrayList<WordCount>(data.size)

        data.forEach {
            list += WordCount(it.key, it.value.value)
        }

        list.sort()
        return list
    }

}

