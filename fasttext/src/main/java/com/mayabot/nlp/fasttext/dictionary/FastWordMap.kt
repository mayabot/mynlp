package com.mayabot.nlp.fasttext.dictionary

import java.lang.Integer.max
import java.lang.Integer.min

/**
 * 每个词和一个WordId关联。快速更具id查找到word。
 * 也就是为Word进行OneHot编码
 */
@ExperimentalUnsignedTypes
class FastWordMap(
        /**
         * 记录的是hash和id的隐射关系
         */
        var wordHash2WordId: IntArray,

        val wordList: ArrayList<Entry>,

        val label: String = "__label__"
) {

    /**
     * 构建一个空的
     */
    constructor(label: String,
                vocabSize: Int,
                initWordListSize: Int?) : this(
            IntArray(vocabSize) { -1 },
            ArrayList(initWordListSize ?: max(1000, min(10000, vocabSize / 1000))),
            label
    )

    val size get() = wordList.size

    operator fun get(id: Int) = wordList[id]

    /**
     * 向词典中新增一个词
     */
    fun add(w: String) {

        val h = find(w)
        val id = wordHash2WordId[h]

        if (id == -1) {
//            wordList.add(Entry(w, 1, getType(w)))
//            word_hash_2_id[h] = size++
            wordHash2WordId[h] = wordList.size
            wordList.add(Entry(w, 1, getType(w)))
        } else {
            wordList[id].count++
        }
    }

    fun getWord(id: Int): String {
        return wordList[id].word
    }


    fun getType(id: Int): EntryType {
        return wordList[id].type
    }

    fun getType(w: String): EntryType {
        return if (w.startsWith(label)) EntryType.label else EntryType.word
    }

    /**
     * word 在wordList里面的下标，也就是词ID。
     *
     * @param w
     * @return
     */
    fun getId(w: String): Int {
        val id = find(w)
        return if (id == -1) {
            -1 //词不存在
        } else wordHash2WordId[id]
    }

    /**
     * word 在wordList里面的下标，也就是词ID。
     */
    fun getId(w: String, h: UInt): Int {
        val id = find(w, h)
        return if (id == -1) {
            -1 //词不存在
        } else wordHash2WordId[id]
    }

    /**
     * 返回的是word_hash_2_id的下标。返回的是不冲突的hash值，也是word_hash的下标索引的位置
     * 原来的find
     * @param w
     * @return 返回的是word_hash_2_id的下标 返回-1标识不存在
     */
    fun find(w: String): Int {
        return find(w, w.fnv1aHash())
    }

    /**
     * 找到word，对应的ID，要么还没人占坑。如果有人占坑了，那么要相等
     * word2int  [index -> words_id]
     * 就是为word在wordList中找到一个正确的下标，最终建立word和
     * Entry的关系。为什么不直接用HashMap呢，业务HashMap的代价太高了吧
     * @param w
     * @param hash
     * @return 返回的是word_hash_2_id的下标 返回-1标识不存在
     */
    fun find(w: String, hash: UInt): Int {
        val word2intSize = wordHash2WordId.size
        var id = (hash.toLong() % word2intSize).toInt()
        while (wordHash2WordId[id] != -1
                && wordList[wordHash2WordId[id]].word != w) {
            id = (id + 1) % word2intSize
        }
        return id
    }

    /**
     * 为wordList里面的每个word进行编码
     */
    fun initWordHash2WordId() {
        for (i in 0 until size) {
            wordHash2WordId[find(wordList[i].word)] = i
        }
    }

    fun collapseWordHash2Id() {
        wordHash2WordId = IntArray((size.toFloat() / 0.75).toInt()) { -1 }
        initWordHash2WordId()
    }


}
