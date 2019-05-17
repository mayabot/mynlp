package com.mayabot.nlp.newword

import com.carrotsearch.hppc.CharIntScatterMap
import com.carrotsearch.hppc.IntHashSet
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.primitives.Ints
import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.collection.dat.DoubleArrayTrieMap
import com.mayabot.nlp.common.ParagraphReaderSmart
import com.mayabot.nlp.segment.lexer.core.CoreDictionary
import com.mayabot.nlp.utils.Characters
import java.io.StringReader
import java.util.*
import kotlin.math.log10
import kotlin.math.log2

/**
 *
 * 新词发现引擎
 *
 * @param maxGroup 发现的词或短语的最小长度
 * @param minGroup 发现的词或短语的最大长度
 * @param minOccurCount 词在预料中出现的最小的次数
 * @author jimichan
 */
class NewWordFindEngine(
        private val minGroup: Int = 3,
        private val maxGroup: Int = 12,
        minOccurCount: Int = 10,
        private val excludeCoreDict: Boolean = true
) {

    var docCount = 0

    val coreDictionary = Mynlps.instanceOf(CoreDictionary::class.java)

    /**
     * 语料中总共有多少字
     */
    var ziCountTotal = 0

    /**
     * 片段的数量
     */
    var partCount = 0

    /**
     * 每个字母的Freq
     */
    val ziFreqArray = IntArray(65535)

    /**
     * 收集频率出现最多的Ngram片断
     */
    private val topWordCounter = TopCounter(2000000, minOccurCount)

    /**
     * 第一轮计算下来的，最有可能的片段
     */
    var candidateMap = HashMap<String, WordInfo>()

    /**
     * 过滤以这些字符开头的片段
     */
    val filterStartChar = IntArray(65535)
    val filterContainsChar = IntArray(65535)


    /**
     * 候选词典. 主要用来第二轮代替hashmap，判断存在
     */
    private var dict = DoubleArrayTrieMap<String>(Maps.newTreeMap(sortedMapOf("a" to "")))

    var verbose = true

    init {
        for (i in 0 until 65535) {
            filterStartChar[i] = 0
            filterContainsChar[i] = 0
            val c = i.toChar()
            if (Characters.isPunctuation(c)) {
                filterStartChar[i] = 1
                filterContainsChar[i] = 1
                continue
            }
            if (Characters.isASCII(c)) {
                filterStartChar[i] = 1
                filterContainsChar[i] = 1
                continue
            }
            if (c.isWhitespace()) {
                filterStartChar[i] = 1
                filterContainsChar[i] = 1
                continue
            }
        }

        "˦�来将就这的了和与想我你他为或是对并以于由有个之在把等再从及"
                .toCharArray().forEach {
                    filterStartChar[it.toInt()] = 1
                }


    }

    /**
     * 返回最终的结果，默认安装score排序
     */
    fun result(minMi: Float = 1f, minEntropy: Float = 1f): ArrayList<NewWord> {
        var resultList = Lists.newArrayListWithExpectedSize<NewWord>(candidateMap.count())

        candidateMap.values.forEach {
            if (it.score < 1000 && it.mi > minMi && it.entropy > minEntropy) {
                resultList.add(NewWord(it.word, it.word.length, it.count, it.doc, it.mi, it.mi_avg, it.entropy, it.idf, it.isBlock)
                        .apply { doScore() })
            }
        }

        resultList.sortByDescending { it.score }

        return resultList
    }

    /**
     * 第一轮扫描。该方法可以被调用多次,建议每次传入一篇文章.
     * 第一轮主要完成基本统计和CRF新词发现
     * @param document
     */
    fun firstScan(document: String) {

        val reader = ParagraphReaderSmart(StringReader(document))
        var line = reader.next()
        while (line != null) {
            val lineCharArray = line.toCharArray()
            val len = lineCharArray.size

            ziCountTotal += len

            //字频
            line.forEach { ch ->
                ziFreqArray[ch.toInt()]++
            }

            //NGram 循环
            for (i in 0 until len) {
                val firstChar = lineCharArray[i].toInt()
                if (filterStartChar[firstChar] == 1) {
                    continue
                }
                for (s in minGroup..maxGroup) {
                    val endIndex = i + s
                    if (endIndex <= len) {
                        //最后一个字也要过滤
                        if (filterStartChar[lineCharArray[endIndex - 1].toInt()] == 1) {
                            continue
                        }

                        var toSkip = false

                        if (s <= 5) {
                            for (j in i until endIndex) {
                                if (filterStartChar[lineCharArray[j].toInt()] == 1) {
                                    toSkip = true
                                    break
                                }
                            }
                        }

                        if (toSkip) {
                            continue
                        }

                        for (j in i until endIndex) {
                            if (filterContainsChar[lineCharArray[j].toInt()] == 1) {
                                toSkip = true
                                break
                            }
                        }


                        if (toSkip) {
                            continue
                        }

                        val word = line.substring(i, endIndex)

                        if (excludeCoreDict && coreDictionary.contains(word)) {
                            continue
                        }

                        topWordCounter.put(word)
                        partCount++
                    }
                }
            }

            line = reader.next()
        }
    }

    fun finishFirst() {
        topWordCounter.clean()

        val treeMap = TreeMap<String, String>()

        for (cursor in topWordCounter.topedMap) {
            if (excludeCoreDict && coreDictionary.contains(cursor.key)) {
                continue
            }
            treeMap[cursor.key!!] = ""
            candidateMap[cursor.key!!] = WordInfo(cursor.key!!)
        }

        dict = DoubleArrayTrieMap<String>(treeMap)
    }


    /**
     * 第二轮扫描。该方法被调用多次,建议每次传入一篇文章.
     * 根据第一轮的基本统计，采用互信息和信息熵的方式计算新词
     */

    fun secondScan(document: String) {
        docCount++
        val reader = ParagraphReaderSmart(StringReader(document))
        var line = reader.next()
        while (line != null) {
            val len = line.length

            //NGram 循环
            for (i in 0 until len) {
                val firstChar = line[i].toInt()
                if (filterStartChar[firstChar] == 1) {
                    continue
                }
                for (s in minGroup..maxGroup) {
                    val toIndex = i + s
                    if (toIndex <= len) {

                        if (dict.get(line, i, toIndex - i) == null) {
                            continue
                        }

                        val word = line.substring(i, toIndex)

                        var info = candidateMap[word]
                        info?.let {
                            info.count++
                            val left = if (i >= 1) line[i - 1] else '^'
                            val right = if (toIndex < len) line[toIndex] else '$'

                            it.left.addTo(left, 1)
                            it.right.addTo(right, 1)
                            it.docSet.add(docCount)
                            if ((left == '“' && right == '”') || (left == '\"' && right == '\"') || (left == '《' && right == '》')) {
                                it.isBlock = true
                            }

                        }
                    }
                }
            }

            line = reader.next()
        }
    }


    fun finishSecond() {

        val dc = docCount.toDouble()
        val zc = ziCountTotal.toDouble()


        candidateMap.values.forEach { info ->
            //计算互信息
            // 在一起的概率
            val fenzi = info.count.toFloat() / ziCountTotal
            var fenmu = 1f
            info.word.forEach { ch ->
                fenmu *= ziFreqArray[ch.toInt()].toFloat() / ziCountTotal
            }

            info.mi = log2(fenzi / fenmu)
            info.mi_avg = info.mi / info.word.length.toFloat()

            //计算左右最小熵
            info.entropy()
            info.tfIdf(dc, zc)

        }
    }
}

/**
 * 词和词数量
 */
class WordCount(val word: String, val count: Int) : Comparable<WordCount> {
    override fun compareTo(other: WordCount): Int {
        return Ints.compare(other.count, count)
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
        val idf: Float,
        val isBlock: Boolean
) {
    var score: Float = 0f

    /**
     * 内置打分公式
     */
    fun doScore() {
        score = avg_mi + entropy + idf * 1.5f + len * freq / docFreq
        if (isBlock) score += 1000
    }
}

class WordInfo(val word: String) {

    companion object {
        val empty = CharIntScatterMap(10)
        val emptySet = IntHashSet(10)
    }

    var count = 0
    var mi = 0f

    var mi_avg = 0f

    // 是否被双引号 书名号包围
    var isBlock = false

    var entropy = 0f

    var left = CharIntScatterMap(10)
    var right = CharIntScatterMap(10)

    var docSet = IntHashSet()

    var score = 0f

    var idf = 0f

    var doc = 0
//    var tfIdf =0f

    fun tfIdf(docCount: Double, ziCount: Double) {
        val doc = docSet.size() + 1
        idf = log10(docCount / doc).toFloat()
//        tfIdf = (idf * (count/ziCount)).toFloat()
        docSet = emptySet
        this.doc = doc - 1
    }

    fun entropy() {

        var leftEntropy = 0f
        for (entry in left) {
            val p = entry.value / count.toFloat()
            leftEntropy -= (p * Math.log(p.toDouble())).toFloat()
        }

        var rightEntropy = 0f
        for (entry in right) {
            val p = entry.value / count.toFloat()
            rightEntropy -= (p * Math.log(p.toDouble())).toFloat()
        }

        entropy = Math.min(leftEntropy, rightEntropy)

        left = empty
        right = empty
    }
}
