package com.mayabot.nlp.starspace

import java.io.*
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern

const val HASH_C = 116049371
const val MAX_VOCAB_SIZE = 30000000

class Dictionary(val args: Args) {

    private val label = args.label

    private var entryList: MutableList<Entry> = ArrayList(50000)
    private var hashToIndex: IntArray = IntArray(MAX_VOCAB_SIZE)

    init {
        Arrays.fill(hashToIndex, -1)
    }

    var size: Int = 0
        private set
    private var nwords: Int = 0
    private var nlabels: Int = 0
    private var ntokens: Long = 0
    private var verbose = true
    private val useWeight = args.useWeight
    private val normalizeText = args.normalizeText
    private val ngrams: Int = args.ngrams
    private val bucket: Int = args.bucket


    private fun hash(str: String): Long {
        // 0xffffffc5;
        var h = 2166136261L.toInt()
        for (strByte in str.toByteArray()) {
            // FNV-1a
            h = (h xor strByte.toInt()) * 16777619
        }

        return h.toLong() and 0xffffffffL
    }

    /**
     * 返回word在hashToIndex数组的下标
     * @param word
     * @return
     */
    private fun find(word: String): Int {
        var h = (hash(word) % MAX_VOCAB_SIZE).toInt()
        while (hashToIndex[h] != -1 && entryList[hashToIndex[h]].symbol != word) {
            h = (h + 1) % MAX_VOCAB_SIZE
        }
        return h
    }

    /**
     * 返回symbot在entryList中的位置
     * @param symbol
     * @return
     */
    fun getId(symbol: String): Int {
        val h = find(symbol)
        return hashToIndex[h]
    }

    fun getSymbol(id: Int): String {

        checkArgument(id >= 0)
        checkArgument(id < size)

        return entryList[id].symbol
    }

    fun getLabel(lid: Int): String {
        checkArgument(lid >= 0)
        checkArgument(lid < nlabels)
        return entryList[lid + nwords].symbol
    }

    fun getType(id: Int): EntryType {
        checkArgument(id >= 0)
        checkArgument(id < size)
        return entryList[id].type
    }

    fun getType(w: String): EntryType {
        return if (w.startsWith(label)) EntryType.Label else EntryType.Word
    }


    private fun insert(symbol: String) {
        val h = find(symbol)
        ntokens++
        if (hashToIndex[h] == -1) {
            val e = Entry(symbol, 1, getType(symbol))
            entryList.add(e)
            hashToIndex[h] = size++
        } else {
            entryList[hashToIndex[h]].count++
        }
    }


    /* Build dictionary from file.
     * In dictionary building process, if the current dictionary is at 75% capacity,
     * it automatically increases the threshold for both Word and Label.
     * At the end the -minCount and -minCountLabel from arguments will be applied
     * as thresholds.
     */
    @Throws(IOException::class)
    fun readFromFile(
        file: String
    ) {

        println("Build dict from input file : $file")

        /**
         *
         * @param t 词出现次数的最小值
         * @param tl 标签出现次数的最小值
         */
        fun threshold(t: Long, tl: Long) {
            entryList =
                entryList.filterNot { it.type == EntryType.Word && it.count < t || it.type == EntryType.Label && it.count < tl }
                    .sortedWith(Comparator<Entry> { o1, o2 -> o1.type.compareTo(o2.type) }.thenByDescending { it.count })
                    .toMutableList()
            (entryList as ArrayList<Entry>).trimToSize()

            computeCounts()
        }


        var minThreshold: Long = 1
        var linesRead = 0
        val over = 0.75 * MAX_VOCAB_SIZE

        File(file).useLines { lines ->
            lines.filterNot { it.isNullOrBlank() }
                .forEach { line ->
                    linesRead++
                    val tokens = parseForDict(line)

                    tokens.forEach { token ->
                        insert(token)
                        if (ntokens % 1000000 == 0L && verbose) {
                            print("\rRead " + ntokens / 1000000 + "M words")
                        }
                        if (size > over) {
                            minThreshold++
                            threshold(minThreshold, minThreshold)
                        }
                    }

                }
        }

        threshold(args.minCount.toLong(), args.minCountLabel.toLong())

        print("\rRead " + ntokens / 1000000 + "M words")
        println("\nNumber of words in dictionary:  $nwords words")
        println("Number of labels in dictionary:  $nlabels words")
        if (linesRead == 0) {
            System.err.println("ERROR: Empty file.")
            System.exit(0)
        }

        if (size == 0) {
            System.err.println("Empty vocabulary. Try a smaller -minCount value.")
            System.exit(0)
        }
    }

    /**
     * takes input as a line of string, output tokens to be added for building the dictionary.
     * @param line
     * @return
     */
    val whitePattern = Pattern.compile("\\s")

    private fun parseForDict(line: String): Iterable<String> {

        var tokens = line.split(whitePattern).asSequence().map { it.trim() }.filterNot { it.isNullOrEmpty() }


        //word_1:wt_1 word_2:wt_2 ... word_k:wt_k __label__1:lwt_1 ...    __label__r:lwt_r
        if (useWeight) {
            tokens = tokens.map {
                val i = it.indexOf('：')
                if (i > 0) it.substring(0, i) else it
            }
        }

        if (normalizeText) {
            tokens = tokens.map { NormalizeText.normalize(it) }
        }

        tokens = tokens.filterNot { it.startsWith("__weight__") }

        return tokens.asIterable()
    }

    // Sort the dictionary by [Word, Label] order and by number of occurance.
    // Removes Word / Label that does not pass respective threshold.


    fun print() {
        for ((_, count) in entryList) {
            if (count > 1) {
                println()
            }
        }
        entryList.forEach(Consumer<Entry> { println(it) })
        println("length $size")
        println("nwords = $nwords")
        println("nlabels = $nlabels")
        println("ntokens = $ntokens")
    }

    private fun computeCounts() {
        size = 0
        nwords = 0
        nlabels = 0

        Arrays.fill(hashToIndex, -1)

        for ((symbol, _, type) in entryList) {
            val h = find(symbol)
            hashToIndex[h] = size++
            if (EntryType.Word === type) {
                nwords++
            } else if (EntryType.Label === type) {
                nlabels++
            }
        }

    }

    // Given a model saved in .tsv format, build the dictionary from model.
    @Throws(IOException::class)
    fun loadDictFromTsvModel(modelfile: String) {
        println("Loading dict from model file : $modelfile")

        File(modelfile).useLines { lines ->
            lines.filterNot { it.isBlank() }
                .forEach { line ->
                    val i = line.indexOf('\t')
                    val symbol = line.substring(0, i)
                    insert(symbol)
                }
        }

        computeCounts()

        println("Number of words in dictionary:  $nwords")
        println("Number of labels in dictionary: $nlabels")
    }

    fun printDoc(tokens: List<XPair>): String {
        val sb = StringBuilder()

        for ((first) in tokens) {
            // skip ngram tokens
            if (first < size) {
                sb.append(getSymbol(first)).append(" ")
            }
        }

        return sb.toString()
    }

    @JvmOverloads
    fun parseDoc(doc: String, sep: Pattern = Pattern.compile("\\s")): List<XPair> {
        return parseDoc(doc.split(sep))
    }

    private fun parseDoc(tokens: List<String>): List<XPair> {
        val rslts = ArrayList<XPair>()
        for (token in tokens) {
            var t = token
            var weight = 1.0f
            if (useWeight) {
                val pos = token.indexOf(':')
                if (pos > 0) {
                    t = token.substring(0, pos)
                    weight = token.substring(pos + 1).toFloat()
                }
            }

            if (normalizeText) {

                t = NormalizeText.normalize(t)
            }
            val wid = getId(t)
            if (wid < 0) {
                continue
            }

            val type = getType(wid)
            if (type === EntryType.Word) {
                rslts.add(XPair(wid, weight))
            }
        }

        if (ngrams > 1) {
            addNgrams(tokens, rslts)
        }

        return rslts
    }

    fun addNgrams(tokens: Iterable<String>, line: MutableList<XPair>) {
        if (ngrams <= 1) {
            return
        }

        val hashes = ArrayList<Long>()
        for (token in tokens) {
            val type = getType(token)
            if (type === EntryType.Word) {
                hashes.add(hash(token))
            }
        }

        val nword_nlabels = nwords() + nlabels()
        for (i in hashes.indices) {
            var h = hashes[i]
            var j = i + 1
            while (j < hashes.size && j < i + ngrams) {
                h = h * HASH_C + hashes[j]
                h = Math.abs(h)
                val id = h % bucket
                line.add(XPair.createPair(nword_nlabels + id, 1.0f))
                j++
            }
        }
    }

    fun nwords(): Int {
        return nwords
    }

    fun nlabels(): Int {
        return nlabels
    }


    @Throws(IOException::class)
    fun save(ofs: OutputStream) {
        val dout = DataOutputStream(ofs)
        dout.writeInt(size)
        dout.writeInt(nwords)
        dout.writeInt(nlabels)
        dout.writeLong(ntokens)

        for (i in 0 until size) {
            val (symbol, count, type) = entryList[i]

            dout.writeUTF(symbol)
            dout.writeLong(count)
            dout.writeInt(type.ordinal)
        }

        dout.flush()
    }

    @Throws(IOException::class)
    fun load(ifs: InputStream): Dictionary {

        val `in` = DataInputStream(ifs)

        size = `in`.readInt()
        nwords = `in`.readInt()
        nlabels = `in`.readInt()
        ntokens = `in`.readLong()

        hashToIndex = IntArray(MAX_VOCAB_SIZE)
        Arrays.fill(hashToIndex, -1)

        entryList = ArrayList(200000)

        val values = EntryType.values()
        for (i in 0 until size) {
            val e = Entry(
                `in`.readUTF(),
                `in`.readLong(),
                values[`in`.readInt()]
            )
            entryList.add(e)
            hashToIndex[find(e.symbol)] = i
        }

        return this
    }
}


data class Entry constructor(
    @JvmField val symbol: String,
    @JvmField var count: Long,
    @JvmField val type: EntryType
)


enum class EntryType {

    Word, Label;
}
