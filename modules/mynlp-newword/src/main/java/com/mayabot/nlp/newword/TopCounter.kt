package com.mayabot.nlp.newword

import com.carrotsearch.hppc.ObjectIntHashMap

/**
 * @author jimichan
 * 统计元素重复最高
 */
class TopCounter(val size: Int = 2000000, var minCount: Int = 10) {

    var verbose = false

    var topedMap = ObjectIntHashMap<String>(size)

    var topList = listOf<String>()


    var lastMinCount = 2

    fun put(key: String) {
        topedMap.putOrAdd(key, 1, 1)

        if (topedMap.size() >= size) {
            reduce()
        }
    }

    private fun reduce() {
        //1. remove count less min
        if (verbose) println("清洗前有${topedMap.size()}条数据")
        val target = size / 4 //压缩为1/4

        var max = 0

        for (min in lastMinCount until minCount) {
            if (topedMap.size() > target) {
                topedMap.removeAll { _, value -> value <= min }
                if (verbose) println("删除小于 ${min} 的数量,剩余${topedMap.size()}")
                max = min
            }
        }

        lastMinCount = max - 1
        if (lastMinCount <= 2) {
            lastMinCount = 2
        }

        //还超出一半
        if (topedMap.size() > size / 2) {
            minCount++
        }

        if (verbose) println("-".repeat(20))
    }

    fun clean() {
        topedMap.removeAll { _, value -> value <= minCount }
    }


    fun getListResult(): List<WordCount> {
        clean()
        val list = ArrayList<WordCount>(topedMap.size())
        topedMap.forEach { cusor ->
            list += WordCount(cusor.key!!, cusor.value)
        }
        list.sort()
        return list
    }

}
