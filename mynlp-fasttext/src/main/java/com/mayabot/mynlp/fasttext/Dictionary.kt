package com.mayabot.mynlp.fasttext

import com.carrotsearch.hppc.IntArrayList
import com.carrotsearch.hppc.IntIntHashMap
import com.carrotsearch.hppc.IntIntMap
import com.carrotsearch.hppc.LongArrayList
import com.google.common.base.CharMatcher
import com.google.common.base.Splitter
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*

const val HASH_C = 116049371
const val MAX_VOCAB_SIZE = 30000000
const val MAX_LINE_SIZE = 1024
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
class Dictionary(private val args: Args) {

    var size: Int = 0
        private set

    var wordList:MutableList<Entry> = ArrayList(50000 * 4)
    private var word_hash_2_id: IntArray = IntArray(MAX_VOCAB_SIZE).apply {
        fill(-1)
    }

    var nwords:Int = 0
    var nlabels:Int = 0
    var ntokens:Long = 0

    var pruneidxSize = -1L
    var pdiscard:FloatArray = FloatArray(0)
    var pruneidx:IntIntMap = IntIntHashMap()

    /**
     * maxn length of char ngram
     */
    val maxn = args.maxn
    val minn = args.minn
    val bucket = args.bucket
    val wordNgrams = args.wordNgrams
    val label = args.label
    val model = args.model


    fun isPruned() = pruneidxSize >=0


    fun getType(id: Int): EntryType {
        checkArgument(id >= 0)
        checkArgument(id < size)
        return wordList[id].type
    }

    fun getType(w: String): EntryType {
        return if (w.startsWith(label)) EntryType.label else EntryType.word
    }

    /**
     * word 在words_里面的下标，也就是词ID。
     *
     * @param w
     * @return
     */
    fun getId(w: String): Int {
        val id = find(w)
        return if (id == -1) {
            -1 //词不存在
        } else word_hash_2_id[id]
    }

    private fun getId(w: String, h:Long): Int {
        val id = find(w,h)
        return if (id == -1) {
            -1 //词不存在
        } else word_hash_2_id[id]
    }

    /**
     * 向词典中新增一个词
     */
    fun add(w: String) {
        val h = find(w)
        val id = word_hash_2_id[h]

        if (id == -1) {
            wordList.add(Entry(w,1,getType(w)))
            word_hash_2_id[h] = size++
        } else {
            wordList[id].count++
        }
        ntokens++
    }

    /**
     * 返回的是word_hash_2_id的下标。返回的是不冲突的hash值，也是word_hash的下标索引的位置
     * 原来的find
     * @param w
     * @return
     */
    private fun find(w: String): Int {
        return find(w, stringHash(w))
    }

    /**
     * 找到word，对应的ID，要么还没人占坑。如果有人占坑了，那么要相等
     * word2int  [index -> words_id]
     *
     * @param w
     * @param hash
     * @return 返回的是word2int的下标
     */
    private fun find(w: String, hash: Long): Int {
        var h = (hash % MAX_VOCAB_SIZE).toInt()
        while (word_hash_2_id[h] != -1 && wordList[word_hash_2_id[h]].word != w) {
            h = (h + 1) % MAX_VOCAB_SIZE
        }
        return h
    }

    private fun stringHash(str: String): Long {
        // 0xffffffc5;
        var h = 2166136261L.toInt()
        for (strByte in str.toByteArray()) {
            // FNV-1a
            h = (h xor strByte.toInt()) * 16777619
        }

        return h.toLong() and 0xffffffffL
    }

    fun getWord(id: Int): String {
        checkArgument(id >= 0)
        checkArgument(id < size)
        return wordList[id].word
    }



    /**
     * 读取分析原始语料，语料单词直接空格
     *
     * @param file 训练文件
     * @throws Exception
     */
    @Throws(Exception::class)
    fun buildFromFile(file: File) {

        val mmm = 0.75 * MAX_VOCAB_SIZE

        //final String lineDelimitingRegex_ = " |\r|\t|\\v|\f|\0";

        var minThreshold: Long = 1

        println("Read file build dictionary ...")

        val splitter = Splitter.on(CharMatcher.whitespace())
                .omitEmptyStrings().trimResults()

        file.useLines { lines ->
            lines.filterNot { it.isNullOrBlank() || it.startsWith("#") }
                    .forEach { line ->
                        splitter.split(line).forEach {
                            token ->
                            add(token)
                            if (ntokens % 1000000 == 0L && args.verbose > 1) {
                                print("\rRead " + ntokens / 1000000 + "M words")
                            }

                            if (size > mmm) {
                                minThreshold++
                                threshold(minThreshold, minThreshold)
                            }
                        }
                        add(EOS)
                    }

            threshold(args.minCount.toLong(), args.minCountLabel.toLong())

            initTableDiscard()
            initNgrams()


            if (args.verbose > 0) {
                System.out.printf("\rRead %dM words\n", ntokens / 1000000)
                println("Number of words:  $nwords")
                println("Number of labels: $nlabels")
            }
            if (size == 0) {
                System.err.println("Empty vocabulary. Try a smaller -minCount second.")
                System.exit(1)
            }
        }


    }

    /**
     * 初始化 char ngrams 也就是 subwords
     */
    private fun initNgrams() {
        for (id in 0 until size) {
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
            //e.subwords.trimToSize();
        }
    }


    private fun computeSubwords(word: String, ngrams: IntArrayList) {
        val word_len = word.length
        for (i in 0 until word_len) {

            if (charMatches(word[i])) {
                continue
            }

            val ngram = StringBuilder()

            var j = i
            var n = 1
            while (j < word_len && n <= maxn) {
                ngram.append(word[j++])
                while (j < word.length && charMatches(word[j])) {
                    ngram.append(word[j++])
                }
                if (n >= minn && !(n == 1 && (i == 0 || j == word.length))) {
                    val h = (stringHash(ngram.toString()) % bucket).toInt()
                    if (h < 0) {
                        System.err.println("computeSubwords h<0: $h on word: $word")
                    }
                    pushHash(ngrams, h)
                }
                n++
            }
        }
    }

    private fun pushHash(hashes: IntArrayList, id: Int) {
        var id = id
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


    private fun charMatches(ch: Char) = ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r'

    fun initTableDiscard() {
        pdiscard = FloatArray(size)
        val t = args.t
        for (i in 0 until size) {
            val f = wordList[i].count * 1.0f / ntokens
            pdiscard[i] = (Math.sqrt(t / f) + t/ f).toFloat()
        }
    }

    fun threshold(t: Long, tl: Long) {
        wordList = wordList.filterNot { it.type == EntryType.word && it.count < t || it.type == EntryType.label && it.count < tl }
                .sortedWith(Comparator<Entry> { o1, o2 -> o1.type.compareTo(o2.type) }.thenByDescending { it.count })
                .toMutableList()
        (wordList as java.util.ArrayList<Entry>).trimToSize()

        size=0
        nwords=0
        nlabels = 0

        word_hash_2_id.fill(-1)

        wordList.forEach {
            val h = find(it.word)
            word_hash_2_id[h] = size++
            if (it.type == EntryType.word) {
                nwords++
            }else if (it.type == EntryType.label) {
                nlabels++
            }
        }
    }


    fun nwords() = nwords
    fun nlabels() = nlabels
    fun ntokens() = ntokens


    fun getCounts(type: EntryType): LongArray {
        val counts = if (EntryType.label == type)
            LongArray(nlabels())
        else
            LongArray(nwords())
        var i = 0
        for ((_, count, type1) in wordList) {
            if (type1 == type)
                counts[i++] = count
        }
        return counts
    }


    fun getLine(tokens: Iterable<String>, words: IntArrayList, labels: IntArrayList): Int {
        val word_hashes = LongArrayList()
        var ntokens = 0

        words.clear()
        labels.clear()

        for (token in tokens) {
            val h = stringHash(token)
            val wid = getId(token, h)
            val type = if (wid < 0) getType(token) else getType(wid)

            ntokens++

            if (type == EntryType.word) {
                addSubwords(words, token, wid)
                word_hashes.add(h)
            } else if (type == EntryType.label && wid >= 0) {
                labels.add(wid - nwords)
            }
        }

        addWordNgrams(words, word_hashes, wordNgrams)

        return ntokens
    }


    private fun addWordNgrams(line: IntArrayList,
                              hashes: LongArrayList,
                              n: Int) {
        for (i in 0 until hashes.size()) {
            var h = hashes.get(i)
            var j = i + 1
            while (j < hashes.size() && j < i + n) {
                h = h * 116049371 + hashes.get(j)
                pushHash(line, (h % bucket).toInt())
                j++
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
        return wordList[id].subwords
    }

    fun getSubwords(word: String): IntArrayList {
        val i = getId(word)

        if (i >= 0) {
            return wordList[i].subwords
        }

        val ngrams = IntArrayList()
        computeSubwords(BOW + word + EOW, ngrams)

        return ngrams
    }

    fun getSubwords(word: String, ngrams: IntArrayList,
                    substrings: MutableList<String>) {
        val i = getId(word)
        ngrams.clear()
        substrings.clear()
        if (i >= 0) {
            ngrams.add(i)
            substrings.add(wordList[i].word)
        } else {
            ngrams.add(-1)
            substrings.add(word)
        }

        computeSubwords(BOW + word + EOW, ngrams)
    }

    fun getLine(tokens: List<String>, words: IntArrayList,
                rng: Random): Int {
        var ntokens = 0
        words.clear()
        for (token in tokens) {
            val h = find(token)
            val wid = word_hash_2_id[h]
            if (wid < 0) continue

            ntokens++

            if (getType(wid) == EntryType.word && !discard(wid, rng.nextFloat())) {
                words.add(wid)
            }
            if (ntokens > MAX_LINE_SIZE || token == EOS) {
                break
            }
        }


        return ntokens
    }

    private fun discard(id: Int, rand: Float): Boolean {
        checkArgument(id >= 0)
        checkArgument(id < nwords)
        return if (model == ModelName.sup) false else rand > pdiscard[id]
    }


    fun getLabel(lid: Int): String {
        checkArgument(lid >= 0)
        checkArgument(lid < nlabels)
        return wordList[lid + nwords].word
    }

    @Throws(IOException::class)
    fun save(channel: FileChannel) {
        channel.writeInt(size)
        channel.writeInt(nwords)
        channel.writeInt(nlabels)
        channel.writeLong(ntokens)
        channel.writeLong(pruneidxSize)

        val buffer = ByteBuffer.allocate(1024*1024)
        val em = buffer.capacity()*0.25f
        for (entry in wordList) {
            buffer.writeUTF(entry.word)
            buffer.putLong(entry.count)
            buffer.put(entry.type.value.toByte())

           if(buffer.remaining() < em){
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

        val buffer2 = ByteBuffer.allocate(pruneidx.size()*4)
        pruneidx.forEach {
            buffer2.putInt(it.key,it.value)
        }
        buffer2.flip()
        channel.write(buffer2)

    }


    @Throws(IOException::class)
    fun load(buffer: AutoDataInput) : Dictionary {
        // wordList.clear();
        // word2int_.clear();

        size = buffer.readInt()
        nwords = buffer.readInt()
        nlabels = buffer.readInt()
        ntokens = buffer.readLong()
        pruneidxSize = buffer.readLong()

        //        word_hash_2_id = new LongIntScatterMap(size_);
        wordList = ArrayList(size)

        //size 189997 18万的词汇
        //val byteArray = ByteArray(1024)
        for (i in 0 until size) {
            val e = Entry(buffer.readUTF(),buffer.readLong(),EntryType.fromValue(buffer.readUnsignedByte().toInt()))
            wordList.add(e)
            word_hash_2_id[find(e.word)] = i
        }

        pruneidx.clear()
        for (i in 0 until pruneidxSize) {
            val first = buffer.readInt()
            val second = buffer.readInt()
            pruneidx.put(first, second)
        }

        initTableDiscard()
        //if (ModelName.cbow == args_.model || ModelName.sg == args_.model) {
        initNgrams()
        //}
        return this
    }
}

/**
 * 返回的是word2int的下标。返回的是不冲突的hash值，也是word_hash的下标索引的位置
 * 原来的find
 * @param w
 * @return
 */

val Empty_IntArrayList = IntArrayList(0)
data class Entry(
        val word: String,
        var count: Long,
        val type: EntryType
) {
    var subwords: IntArrayList = Empty_IntArrayList
}


enum class EntryType constructor(var value: Int) {

    word(0), label(1);

    override fun toString(): String {
        return if (value == 0) "word" else if (value == 1) "label" else "unknown"
    }

    companion object {

        internal var types = EntryType.values()

        @Throws(IllegalArgumentException::class)
        fun fromValue(value: Int): EntryType {
            try {
                return types[value]
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw IllegalArgumentException("Unknown EntryType enum second :$value")
            }

        }
    }
}