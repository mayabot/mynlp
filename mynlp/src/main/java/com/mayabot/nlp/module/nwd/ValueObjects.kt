package com.mayabot.nlp.module.nwd

import kotlin.math.*

class IntCount {
    var value : Int = 1
}

/**
 * 词和词数量
 */
class WordCount(val word: String, val count: Int) : Comparable<WordCount> {
    override fun compareTo(other: WordCount): Int {
        return other.count.compareTo(count)
    }
}

data class NewWord(
        val word: String,
        val len: Int,
        val freq: Int,
        val docFreq: Int,
        val mi: Float,
        val avg_mi: Float,
        val entropy: Float,
        val le: Float,
        val re: Float,
        val idf: Float,
        val isBlock: Boolean
) {
    var score: Float = 0f

    /**
     * 内置打分公式
     */
    fun doScore() {
        var ac = abs(le - re)
        if (ac == 0.0f) {
            ac = 0.00000000001f
        }
        score = avg_mi + log2((le * exp(re) + re * exp(le)) / ac)
//        score = avg_mi + entropy + idf * 1.5f + len * freq / docFreq
        if (isBlock) score += 1000
    }
}

fun HashMap<Char, IntCount>.addTo(key: Char, count: Int) {
    this.getOrPut(key) {
        IntCount()
    }.value += count
}

class WordInfo(val word: String) {

    companion object {
        val empty = HashMap<Char, IntCount>()
        val emptySet = HashSet<Int>()
    }

    var count = 0
    var mi = 0f

    var mi_avg = 0f

    // 是否被双引号 书名号包围
    var isBlock = false

    var entropy = 0f

    var left = HashMap<Char, IntCount>(10)
    var right = HashMap<Char, IntCount>(10)

    var docSet = HashSet<Int>()

    var score = 0f

    var idf = 0f

    var doc = 0

    var le = 0f
    var re = 0f

//    var tfIdf =0f

    fun tfIdf(docCount: Double, ziCount: Double) {
        val doc = docSet.size + 1
        idf = log10(docCount / doc).toFloat()
//        tfIdf = (idf * (count/ziCount)).toFloat()
        docSet = emptySet
        this.doc = doc - 1
    }

    fun entropy() {

        var leftEntropy = 0f
        for (entry in left) {
            val p = entry.value.value / count.toFloat()
            leftEntropy -= (p * ln(p.toDouble())).toFloat()
        }

        var rightEntropy = 0f
        for (entry in right) {
            val p = entry.value.value / count.toFloat()
            rightEntropy -= (p * ln(p.toDouble())).toFloat()
        }

        le = leftEntropy
        re = rightEntropy

        entropy = min(leftEntropy, rightEntropy)

        left = empty
        right = empty
    }

}
