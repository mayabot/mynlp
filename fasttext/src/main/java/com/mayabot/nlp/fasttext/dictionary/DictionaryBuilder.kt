package com.mayabot.nlp.fasttext.dictionary

import com.mayabot.nlp.fasttext.args.ComputedTrainArgs
import java.util.ArrayList
import kotlin.Comparator
import kotlin.ExperimentalUnsignedTypes
import kotlin.Int
import kotlin.Long
import kotlin.String

/**
 * 字典
 * 分层
 * [
 * words,
 * labels,
 * bucket
 * ]
 *
 * 目前的代码看来，labels和bucket是互斥的，只能存在一个
 *
 * @author jimichan
 */
@ExperimentalUnsignedTypes
class DictionaryBuilder(
        label: String,
        /**
         * 这个肯定要比initWordListSize数量大两个数量级吧
         */
        vocabSize: Int = MAX_VOCAB_SIZE,
        initWordListSize: Int? = null
) {

    val wordIdMap = FastWordMap(label, vocabSize, initWordListSize)

    /**
     * 一个有多少个词（不是排重）
     */
    var ntokens: Long = 0

    /**
     * word的排重的数量
     */
    var nwords: Int = 0

    /**
     * label数量
     */
    var nlabels: Int = 0

    fun toDictionary(args: ComputedTrainArgs): Dictionary {
        return Dictionary(
                args = args.modelArgs,
                onehotMap = wordIdMap,
                ntokens = ntokens,
                nwords = nwords,
                nlabels = nlabels
        )
    }

    val size get() = wordIdMap.size

    fun add(word: String) {
        wordIdMap.add(word)
        ntokens++
    }

    /**
     * 截断
     */
    fun threshold(t: Long, minLabelCount: Long) {

        val wordList = wordIdMap.wordList
                .filterNot { (it.type == EntryType.word && it.count < t) || (it.type == EntryType.label && it.count < minLabelCount) }
                .sortedWith(Comparator<Entry> { o1, o2 -> o1.type.compareTo(o2.type) }
                        .thenByDescending { it.count })
                .toMutableList()
        (wordList as ArrayList<Entry>).trimToSize()

        nwords = 0
        nlabels = 0

        wordIdMap.wordList.clear()
        wordIdMap.wordList.addAll(wordList)

        val word_hash_2_id = wordIdMap.wordHash2WordId
        word_hash_2_id.fill(-1)

        for ((index, entry) in wordList.withIndex()) {
            val h = wordIdMap.find(entry.word)
            word_hash_2_id[h] = index
            if (entry.type == EntryType.word) {
                nwords++
            } else if (entry.type == EntryType.label) {
                nlabels++
            }
        }

    }



}
