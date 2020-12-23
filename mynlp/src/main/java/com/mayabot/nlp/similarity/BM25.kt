package com.mayabot.nlp.similarity

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.LexerReader
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log10

/**
 * BM25相关度评分和检索。
 *
 * 结论：BM25和向量模型，没法转换为 0~1的的归一化的相似度。
 */
class BM25(
    private val k: Float,
    private val b: Float,
    private val avgDocLength: Float,
    private val totalDocCount: Int,
    private val termInfoMap: Map<String, TermInfo>,
    private val docLenMap: Map<Int, Int>,
    private val lexer: LexerReader
) {

    private val MockIDF = ((totalDocCount - 1 + 0.5) / (1 + 0.5f)).toFloat()

    fun guessMaxScore(terms: List<String>): Float {
        var score = 0f
        val qlen = terms.size

        terms.forEach {
            val info = termInfoMap[it]
            if (info == null) {
                score += MockIDF * rscore(1, qlen)
            } else {
                score += info.idf * rscore(1, qlen)
            }
        }
        return score
    }


    /**
     * @param query
     * @param threshold 相关度最低阈值
     */
    @JvmOverloads
    fun search(query: String, k: Int = 10): List<HitResult> {
        val terms = lexer.scan(query).toSentence().toWordList()
        val maxScore = guessMaxScore(terms)

        val docIdBitSet = BitSet(totalDocCount)

        val termVector = ArrayList<TermInfo>()
        terms.forEach {
            val info = termInfoMap.get(it)
            if (info != null) {
                termVector.add(info)
                docIdBitSet.or(info.bitSet)
            }
        }

        var i = docIdBitSet.nextSetBit(0)
        val result = ArrayList<HitResult>()
        while (i >= 0) {
            if (i == Integer.MAX_VALUE) {
                break
            }
            val docId = i
            val bm25score = score(termVector, docId);
            val score = bm25score / maxScore

            result += HitResult(docId, bm25score, score)

            i = docIdBitSet.nextSetBit(i + 1)
        }

        // TODO 使用top堆来减少计算
        return result.sortedByDescending { it.score }.take(k)
    }

    private fun score(termVector: List<TermInfo>, docId: Int): Float {
        var score = 0f
        termVector.forEach { info ->
            val fq = info.docFreqMap.getOrDefault(docId, 0)
            if (fq == 0) {
                return@forEach
            }
            score += info.idf * rscore(fq, docLenMap.getValue(docId))
        }
        return score
    }

    /**
     * qi和当前doc的相关度
     * fq = q 在doc中频次
     */
    private inline fun rscore(fq: Int, docLen: Int): Float {
        return fq * (k + 1) / (fq + k * (1 - b + b * docLen / avgDocLength))
    }

    data class HitResult(
        val docId: Int,
        val bm25Score: Float,
        val score: Float
    )

    class TermInfo(
        val word: String,
        val docFreq: Int,
        val docFreqMap: Map<Int, Int>,
        val freq: Int,
        val N: Int
    ) {
        /**
         * 预先计算好IDF部分
         */
        val idf = log10((N - docFreq + 0.5) / (docFreq + 0.5)).toFloat()

        val docIds = docFreqMap.keys.toSet()

        val bitSet = BitSet(N).apply {
            docIds.forEach {
                this.set(it)
            }
        }
    }
}


private fun defaultLexer() = Mynlp.instance()
        .lexerBuilder().bigram().build()
        .filterReader(true, false)

class BM25ModelBuilder(val docList: List<String>,
                       val lexer: LexerReader = defaultLexer()) {

    private val N = docList.size

    var k = 1.2f

    var b = 0.75f

    fun k(k: Float): BM25ModelBuilder {
        this.k = k
        return this
    }

    fun b(b: Float): BM25ModelBuilder {
        this.b = b
        return this
    }

    fun build(): BM25 {
        val termInfoMap = TreeMap<String, MutableTermInfo>()

        val docLenMap = HashMap<Int, Int>()

        var docLen = 0
        docList.forEachIndexed { docId, doc ->
            val list = lexer.scan(doc).toWordSequence().toList()
            docLen += list.size
            docLenMap[docId] = list.size
            val word2Freq = list.groupingBy { it }.eachCount()
            word2Freq.forEach { (word, freq) ->
                val info = termInfoMap.getOrPut(word) { MutableTermInfo() }
                info.docFreq++
                info.docFreqMap[docId] = freq
                info.freq += freq
            }
        }

        return BM25(k, b, N.toFloat(), docLen / N, termInfoMap.mapValues {
            BM25.TermInfo(it.key, it.value.docFreq, it.value.docFreqMap, it.value.freq, N)
        }, docLenMap, lexer)
    }

    private class MutableTermInfo {
        var docFreq: Int = 0
        val docFreqMap = HashMap<Int, Int>()
        var freq: Int = 0

    }
}

