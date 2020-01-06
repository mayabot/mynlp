package com.mayabot.nlp.fasttext.dictionary

import com.carrotsearch.hppc.IntArrayList
import com.carrotsearch.hppc.IntIntHashMap
import com.carrotsearch.hppc.IntIntMap
import com.carrotsearch.hppc.LongArrayList
import com.google.common.base.Preconditions.checkArgument
import com.mayabot.nlp.fasttext.args.ModelArgs
import com.mayabot.nlp.fasttext.args.ModelName
import com.mayabot.nlp.fasttext.utils.AutoDataInput
import com.mayabot.nlp.fasttext.utils.writeInt
import com.mayabot.nlp.fasttext.utils.writeLong
import com.mayabot.nlp.fasttext.utils.writeUTF
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min
import kotlin.random.Random


const val HASH_C = 116049371
const val MAX_VOCAB_SIZE = 30000000
const val MAX_LINE_SIZE = 1024


const val coeff: ULong = 116049371u
const val U64_START: ULong = 18446744069414584320u

/**
 * 句子的结尾
 * end of sentence
 */
const val EOS = "</s>"

/**
 * begin of word
 */
const val BOW = "<"

/**
 * end of word
 */
const val EOW = ">"

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
class Dictionary(
        val args: ModelArgs,
        val onehotMap: FastWordMap,
        val ntokens: Long,
        val nwords: Int,
        val nlabels: Int
) {

    private var pdiscard: FloatArray = FloatArray(0)
    var pruneidxSize = -1L
    private val pruneidx: IntIntMap = IntIntHashMap()

    private val maxn = args.maxn
    private val minn = args.minn
    private val bucket = args.bucket
    private val bucketULong = bucket.toULong()
    private val wordNgrams = args.wordNgrams

    val size get() = onehotMap.size


//    val bucket = args.bucket
//    val bucketLong = args.bucket.toLong()
//    val bucketULong = args.bucket.toULong()

    fun isPruned() = pruneidxSize >= 0

    private fun isWhiteSpaceChar(ch: Char) = ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r'

    /**
     * 统计每个label或者word的数量，变成一个一维向量
     */
    fun getCounts(type: EntryType): LongArray {
        val counts = if (EntryType.label == type)
            LongArray(nlabels)
        else
            LongArray(nwords)
        var i = 0
        for ((_, count, type1) in onehotMap.wordList) {
            if (type1 == type)
                counts[i++] = count
        }
        return counts
    }


    /**
     * tokens 是一系列的单词或者label。
     * labels存放的是label对应的id向量，从0开始
     * words里面存放了
     */
    fun getLine(tokens: Iterable<String>, words: IntArrayList, labels: IntArrayList): Int {
        val word_hashes = LongArrayList()
        var ntokens = 0

        words.clear()
        labels.clear()

        for (token in tokens) {
            val h = token.fnv1aHash()
            val wid = onehotMap.getId(token, h)
            val type = if (wid < 0) onehotMap.getType(token) else onehotMap.getType(wid)

            ntokens++

            if (type == EntryType.word) {
                addSubwords(words, token, wid)
                word_hashes.add(h.toLong())
            } else if (type == EntryType.label && wid >= 0) {
                labels.add(wid - nwords)
            }
        }

        addWordNgrams(words, word_hashes, wordNgrams)

        return ntokens
    }

    fun getLine(tokens: List<String>, words: IntArrayList, rng: Random): Int {
        var ntokens = 0
        words.clear()
        for (token in tokens) {
            val h = onehotMap.find(token)
            val wid = onehotMap.wordHash2WordId[h]
            if (wid < 0) continue

            ntokens++

            if (onehotMap.getType(wid) == EntryType.word && !discard(wid, rng.nextFloat())) {
                words.add(wid)
            }
            if (ntokens > MAX_LINE_SIZE || token == EOS) {
                break
            }
        }


        return ntokens
    }


    private fun addWordNgrams(line: IntArrayList,
                              hashes: LongArrayList,
                              n: Int) {

        val hashSize = hashes.size()

        for (i in 0 until hashSize) {
            var h = hashes.get(i).toULong()

            for (j in i + 1 until min(hashSize, i + n)) {
                h = h * coeff + hashes.get(j).toULong()
                pushHash(line, (h % bucketULong).toInt())
            }
        }
    }


    private fun addSubwords(line: IntArrayList,
                            token: String,
                            wid: Int) {
        if (wid < 0) { // out of vocab
            if (EOS != token) {
                computeSubwords(BOW + token + EOW, line)
            }
        } else {
            if (maxn <= 0) { // in vocab w/o subwords
                line.add(wid)
            } else { // in vocab w/ subwords
                val ngrams = getSubwords(wid)
                line.addAll(ngrams)
            }
        }
    }

    fun getSubwords(id: Int): IntArrayList {
        checkArgument(id >= 0)
        checkArgument(id < nwords)
        return onehotMap[id].subwords
    }

    fun getSubwords(word: String): IntArrayList {
        val i = onehotMap.getId(word)

        if (i >= 0) {
            return onehotMap[i].subwords
        }

        val ngrams = IntArrayList()

        if (word != EOS) {
            computeSubwords(BOW + word + EOW, ngrams)
        }

        return ngrams
    }

    fun getSubwords(word: String, ngrams: IntArrayList, substrings: MutableList<String>) {
        val i = onehotMap.getId(word)
        ngrams.clear()
        substrings.clear()
        if (i >= 0) {
            ngrams.add(i)
            substrings.add(onehotMap[i].word)
        } else {
            ngrams.add(-1)
            substrings.add(word)
        }

        if (word != EOS) {
            computeSubwords(BOW + word + EOW, ngrams)
        }
    }


    private fun discard(id: Int, rand: Float): Boolean {
        checkArgument(id >= 0)
        checkArgument(id < nwords)
        return if (args.model == ModelName.sup) false else rand > pdiscard[id]
    }


    fun getLabel(lid: Int): String {
        checkArgument(lid >= 0)
        checkArgument(lid < nlabels)
        return onehotMap[lid + nwords].word
    }

    operator fun get(word: String) = onehotMap.getId(word)
    fun getWordId(word: String) = onehotMap.getId(word)

    fun init() {
        initTableDiscard()
        initNgrams()
    }

    fun initTableDiscard() {
        val pdiscard = FloatArray(onehotMap.size)
        val t = args.t
        val wordList = onehotMap.wordList
        for (i in 0 until onehotMap.size) {
            val f = wordList[i].count * 1.0f / ntokens
            pdiscard[i] = (kotlin.math.sqrt(t / f) + t / f).toFloat()
        }
        this.pdiscard = pdiscard
    }

    /**
     * 初始化 char ngrams 也就是 subwords
     */
    fun initNgrams() {
        val wordList = onehotMap.wordList
        for (id in 0 until onehotMap.size) {
            val e = wordList[id]
            val word = BOW + e.word + EOW

            if (maxn == 0) {
                //优化 maxn 一定没有subwords ，这个是分类模型里面的默认定义
                e.subwords = IntArrayList.from(id)
            } else {
                e.subwords = IntArrayList(1)
                e.subwords.add(id)

                if (e.word != EOS) {
                    computeSubwords(word, e.subwords)
                }
            }
        }
    }

    private fun computeSubwords(word: String, ngrams: IntArrayList) {
        val word_len = word.length
        for (i in 0 until word_len) {

            if (isWhiteSpaceChar(word[i])) {
                continue
            }

            var ngram: StringBuilder? = null

            var j = i
            var n = 1
            while (j < word_len && n <= maxn) {
                if (ngram == null) {
                    ngram = StringBuilder()
                }
                ngram.append(word[j++])
                while (j < word.length && isWhiteSpaceChar(word[j])) {
                    ngram.append(word[j++])
                }
                if (n >= minn && !(n == 1 && (i == 0 || j == word.length))) {
                    val h = (ngram.toString().fnv1aHash().toLong() % bucket).toInt()
                    if (h < 0) {
                        System.err.println("computeSubwords h<0: $h on word: $word")
                    }
                    pushHash(ngrams, h)
                }
                n++
            }
        }
    }

    private fun pushHash(hashes: IntArrayList, id_: Int) {
        var id = id_
        if (pruneidxSize == 0L || id < 0) return

        if (pruneidxSize > 0) {
            if (pruneidx.containsKey(id)) {
                id = pruneidx.get(id)
            } else {
                return
            }
        }

        hashes.add(nwords + id)
    }

    @Throws(IOException::class)
    fun save(channel: FileChannel) {
        channel.writeInt(this.size)
        channel.writeInt(nwords)
        channel.writeInt(nlabels)
        channel.writeLong(ntokens)
        channel.writeLong(pruneidxSize)

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val em = buffer.capacity() * 0.25f
        val wordList = onehotMap.wordList
        for (entry in wordList) {
            buffer.writeUTF(entry.word)
            buffer.putLong(entry.count)
            buffer.put(entry.type.value.toByte())

            if (buffer.remaining() < em) {
                buffer.flip()
                while (buffer.hasRemaining()) {
                    channel.write(buffer)
                }
                buffer.clear()
            }
        }

        buffer.flip()
        while (buffer.hasRemaining()) {
            channel.write(buffer)
        }

        val buffer2 = ByteBuffer.allocate(pruneidx.size() * 4)
        pruneidx.forEach {
            buffer2.putInt(it.key, it.value)
        }
        buffer2.flip()
        channel.write(buffer2)

    }

    fun getWord(wid: Int) = onehotMap.getWord(wid)

    fun getWordEntity(wid: Int) = onehotMap.get(wid)

    companion object {

        @Throws(IOException::class)
        fun loadModel(args: ModelArgs, buffer: AutoDataInput): Dictionary {
            // wordList.clear();
            // word2int_.clear();

            val size = buffer.readInt()
            val nwords = buffer.readInt()
            val nlabels = buffer.readInt()
            val ntokens = buffer.readLong()
            val pruneidxSize = buffer.readLong()

            //        word_hash_2_id = new LongIntScatterMap(size_);
            val wordList = ArrayList<Entry>(size)

            for (i in 0 until size) {
                val e = Entry(buffer.readUTF(), buffer.readLong(), EntryType.fromValue(buffer.readUnsignedByte().toInt()))
                wordList.add(e)
            }

            val pruneidx = HashMap<Int, Int>()
            for (i in 0 until pruneidxSize) {
                val first = buffer.readInt()
                val second = buffer.readInt()
                pruneidx.put(first, second)
            }

            // 这里的实际WordHash2WordId是词数量的0.75倍
            val dict = Dictionary(args,
                    FastWordMap(
                            IntArray((size.toFloat() / 0.75).toInt()) { -1 },
                            wordList),
                    ntokens,
                    nwords,
                    nlabels
            )

            dict.initTableDiscard()
            dict.initNgrams()

            dict.onehotMap.initWordHash2WordId()

            return dict
        }

    }
}
