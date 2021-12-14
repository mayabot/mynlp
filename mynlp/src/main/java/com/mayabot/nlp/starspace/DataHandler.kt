package com.mayabot.nlp.starspace

import com.mayabot.nlp.starspace.TrainMode.*
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors.toList

/**
 * 存放训练数据的地方
 */
interface DataHandler {

    @Throws(IOException::class)
    fun loadFromFile(fileName: String)

    @Throws(IOException::class)
    fun save(out: OutputStream)

    // In the case of trainMode=1, a random Label from r.h.s will be selected
    fun convert(example: ParseResult, rslt: ParseResult)

    fun getWordExamples(idx: Int, rslts: MutableList<ParseResult>)
    fun getWordExamples(doc: List<XPair>, rslts: MutableList<ParseResult>)
    fun addExample(example: ParseResult)
    fun getExampleById(idx: Int, rslt: ParseResult)
    fun getNextExample(rslt: ParseResult)
    fun getRandomExample(rslt: ParseResult)
    fun getKRandomExamples(K: Int, c: MutableList<ParseResult>)
    fun getNextKExamples(K: Int, c: MutableList<ParseResult>)

    // Randomly sample one example and randomly sample a Label from this example
// The result is usually used as negative samples in training
    fun getRandomRHS(results: MutableList<XPair>, trainWord: Boolean)

    fun size(): Int
}

open class InternDataHandler(val parser: DataParser, val args: Args) : DataHandler {

    //    public final Args trainArgs;

    var examples: MutableList<ParseResult> = ArrayList()

    var idx = -1
    var size = 0

    // Convert an example for training/testing if needed.
    var random = Random()

    private fun errorOnZeroExample(fileName: String) {
        val string = ("ERROR: File '" + fileName
                + "' does not contain any valid example.\n"
                + "Please check: is the file empty? "
                + "Do the examples contain proper feature and Label according to the trainMode? "
                + "If your examples are unlabeled, try to set trainMode=5.\n")
        println(string)
        System.exit(0)
    }

    override fun size() = size

    @Throws(IOException::class)
    override fun loadFromFile(fileName: String) {

        println("Loading data from file : $fileName")

        val sep = Pattern.compile("[\t ]")

        examples =
            fileName.toFile().lines().parallel().map { parser.parse(it, sep) }.filter({ it != null }).collect(toList())

        println("Total number of examples loaded : " + examples.size)
        size = examples.size
        if (size == 0) {
            errorOnZeroExample(fileName)
        }
    }

    /**
     * 随机挑选一个list里面的值
     */
    protected fun <T> List<T>.rand() = this[random.nextInt(size)]

    protected fun <T> List<T>.randIndex() = random.nextInt(size)

    // In the case of trainMode=1, a random Label from r.h.s will be selected
    override fun convert(example: ParseResult, rslt: ParseResult) {

        val lhsTokens = ArrayList<XPair>()
        val rhsTokens = ArrayList<XPair>()
        rslt.lhsTokens = lhsTokens
        rslt.rhsTokens = rhsTokens
        rslt.weight = example.weight

        val trainMode = args.trainMode

        lhsTokens.addAll(example.lhsTokens)

        if (trainMode === TrainMode.Mode0) {
            // lhs is the same, pick one random Label as rhs
            assert(example.lhsTokens.isNotEmpty())
            assert(example.rhsTokens.isNotEmpty())

            rhsTokens += example.rhsTokens.rand()

        } else {
            assert(example.rhsTokens.size > 1)
            when {
                trainMode === Mode1 -> {
                    // pick one random Label as rhs and the rest is lhs
                    val idx = example.rhsTokens.randIndex()

                    for (i in example.rhsTokens.indices) {
                        val tok = example.rhsTokens[i]
                        if (i == idx) {
                            rhsTokens.add(tok)
                        } else {
                            lhsTokens.add(tok)
                        }
                    }
                }
                trainMode === Mode2 -> {
                    // pick one random Label as lhs and the rest is rhs
                    val idx = example.rhsTokens.randIndex()

                    for (i in example.rhsTokens.indices) {
                        val tok = example.rhsTokens[i]
                        if (i == idx) {
                            lhsTokens.add(tok)
                        } else {
                            rhsTokens.add(tok)
                        }
                    }
                }
                trainMode === Mode3 -> {
                    // pick two random labels, one as lhs and the other as rhs
                    if (example.rhsTokens.size == 2) {
                        val idx = example.rhsTokens.randIndex()
                        val idx2 = when (idx) {
                            0 -> 1
                            1 -> 0
                            else -> 0
                        }
                        lhsTokens.add(example.rhsTokens[idx])
                        rhsTokens.add(example.rhsTokens[idx2])
                    } else {
                        val idx = example.rhsTokens.randIndex()

                        var idx2: Int
                        do {
                            idx2 = example.rhsTokens.randIndex()
                        } while (idx2 == idx)

                        lhsTokens.add(example.rhsTokens[idx])
                        rhsTokens.add(example.rhsTokens[idx2])
                    }

                }
                trainMode === Mode4 -> {
                    // the first one as lhs and the second one as rhs
                    rhsTokens.add(example.rhsTokens[1])
                    lhsTokens.add(example.rhsTokens[0])
                }
            }
        }
    }

    override fun getWordExamples(idx: Int, rslts: MutableList<ParseResult>) {
        getWordExamples(examples[idx].lhsTokens, rslts)
    }

    override fun getWordExamples(doc: List<XPair>, rslts: MutableList<ParseResult>) {
        val ws = args.ws

        rslts.clear()

        for (widx in doc.indices) {
            val rslt = ParseResult()
            val lhsTokens = ArrayList<XPair>()
            rslt.lhsTokens = lhsTokens

            val rhsTokens = ArrayList<XPair>()
            rslt.rhsTokens = rhsTokens

            rhsTokens.add(doc[widx])

            for (i in Math.max(widx - ws, 0) until Math.min(widx + ws, doc.size)) {
                if (i != widx) {
                    lhsTokens.add(doc[i])
                }
            }
            rslt.weight = args.wordWeight
            rslts.add(rslt)
        }
    }

    override fun addExample(example: ParseResult) {
        examples.add(example)
        size++
    }

    override fun getExampleById(idx: Int, rslt: ParseResult) {
        checkArgument(idx < size)
        convert(examples[idx], rslt)
    }

    override fun getNextExample(rslt: ParseResult) {
        idx++
        if (idx >= size) {
            idx -= size
        }
        convert(examples[idx], rslt)
    }


    override fun getRandomExample(rslt: ParseResult) {
        convert(examples.rand(), rslt)
    }

    override fun getKRandomExamples(K: Int, c: MutableList<ParseResult>) {
        val kSamples = Math.min(K, size)
        for (i in 0 until kSamples) {
            val example = ParseResult()
            getRandomExample(example)
            c.add(example)
        }
    }

    override fun getNextKExamples(K: Int, c: MutableList<ParseResult>) {
        val kSamples = Math.min(K, size)
        for (i in 0 until kSamples) {
            idx = (idx + 1) % size
            val example = ParseResult()
            convert(examples[idx], example)
            c.add(example)
        }
    }

    // Randomly sample one example and randomly sample a Label from this example
// The result is usually used as negative samples in training
    override fun getRandomRHS(results: MutableList<XPair>, trainWord: Boolean) {
        results.clear()

        val ex = examples.rand()

        if (args.trainMode == Mode5 || trainWord) {
            results.add(ex.lhsTokens.rand())
        } else {
            val r = ex.rhsTokens.randIndex()
            if (args.trainMode === Mode2) {
                for (i in ex.rhsTokens.indices) {
                    if (i != r) {
                        results.add(ex.rhsTokens[i])
                    }
                }
            } else {
                results.add(ex.rhsTokens[r])
            }
        }
    }

    @Throws(IOException::class)
    override fun save(out: OutputStream) {
        val writer = OutputStreamWriter(out)

        writer.append("data length : $size").append("\n")

        for (example in examples) {
            writer.append("lhs : ")
            for ((first, second) in example.lhsTokens) {
                writer.append(first.toString())
                writer.append(":")
                writer.append(second.toString())
            }
            writer.append("\n")
            writer.append("rhs : ")
            for ((first, second) in example.rhsTokens) {
                writer.append(first.toString())
                writer.append(":")
                writer.append(second.toString())
            }
            writer.append("\n")
        }

        writer.flush()
    }

}

/**
 * 多层次的。也就是说  word1 word2 <TAB> word1 word2
 * 这个doc lab形式
 */
class LayerDataHandler(parser: DataParser, args: Args) : InternDataHandler(parser, args), DataHandler {

    private val dropoutLHS = args.dropoutLHS
    private val dropoutRHS = args.dropoutRHS

    private fun insert(
        rslt: MutableList<XPair>,
        ex: List<XPair>, dropout: Double
    ) {
        if (dropout < 1e-8) {
            // if dropout is not enabled, copy all elements
            rslt.addAll(ex)
        } else {
            val random = randomTL.get()
            for (it in ex) {
                // dropout 0.7
                if (random.nextFloat() > dropout) {
                    rslt.add(it)
                }
            }
        }
    }

    override fun getWordExamples(
        idx: Int,
        rslts: MutableList<ParseResult>
    ) {

        checkArgument(idx < size)
        val example = examples[idx]
        checkArgument(example.rhsFeatures.isNotEmpty())

        // take one random sentence and train on Word
        super.getWordExamples(example.rhsFeatures.rand(), rslts)
    }

    override fun convert(example: ParseResult, rslt: ParseResult) {

        val trainMode = args.trainMode

        rslt.weight = example.weight
        val lhsTokens = ArrayList<XPair>()
        val rhsTokens = ArrayList<XPair>()
        rslt.lhsTokens = lhsTokens
        rslt.rhsTokens = rhsTokens

        if (trainMode === Mode0) {
            assert(example.lhsTokens.isNotEmpty())
            assert(example.rhsFeatures.isNotEmpty())

            insert(lhsTokens, example.lhsTokens, dropoutLHS)

            insert(rhsTokens, example.rhsFeatures.rand(), dropoutRHS)
        } else {
            assert(example.rhsFeatures.size > 1)
            if (trainMode === Mode1) {
                // pick one random rhs as Label, the rest becomes lhs features
                val idx = example.rhsFeatures.randIndex()
                for (i in example.rhsFeatures.indices) {
                    if (i == idx) {
                        insert(rhsTokens, example.rhsFeatures[i], dropoutRHS)
                    } else {
                        insert(lhsTokens, example.rhsFeatures[i], dropoutLHS)
                    }
                }
            } else if (trainMode === Mode2) {
                // pick one random rhs as lhs, the rest becomes rhs features
                val idx = example.rhsFeatures.randIndex()
                for (i in example.rhsFeatures.indices) {
                    if (i == idx) {
                        insert(lhsTokens, example.rhsFeatures[i], dropoutLHS)
                    } else {
                        insert(rhsTokens, example.rhsFeatures[i], dropoutRHS)
                    }
                }
            } else if (trainMode === Mode3) {
                // pick one random rhs as input
                val idx = example.rhsFeatures.randIndex()
                insert(lhsTokens, example.rhsFeatures[idx], dropoutLHS)
                // pick another random rhs as Label
                var idx2: Int
                do {
                    idx2 = example.rhsFeatures.randIndex()
                } while (idx == idx2)
                insert(rhsTokens, example.rhsFeatures[idx2], dropoutRHS)
            } else if (trainMode === Mode4) {
                // the first one as lhs and the second one as rhs
                insert(lhsTokens, example.rhsFeatures[0], dropoutLHS)
                insert(rhsTokens, example.rhsFeatures[1], dropoutRHS)
            }
        }
    }


    override fun getRandomRHS(results: MutableList<XPair>, trainWord: Boolean) {
        assert(size > 0)
        val ex = examples.rand()

        val r = ex.rhsFeatures.randIndex()

        results.clear()
        val trainMode = args.trainMode

        if (trainMode === Mode5 || trainWord) {
            // pick random Word
            val wid = ex.rhsFeatures[r].randIndex()
            results.add(ex.rhsFeatures[r][wid])

        } else if (trainMode === Mode2) {
            // pick one random, the rest is rhs features
            for (i in ex.rhsFeatures.indices) {
                if (i != r) {
                    insert(results, ex.rhsFeatures[i], dropoutRHS)
                }
            }
        } else {
            insert(results, ex.rhsFeatures[r], dropoutLHS)
        }
    }

    @Throws(IOException::class)
    override fun save(out: OutputStream) {
        val writer = OutputStreamWriter(out)

        writer.append("data length : $size").append("\n")

        for (example in examples) {
            writer.append("lhs : ")
            for (t in example.lhsTokens) {
                writer.append(t.first.toString())
                writer.append(":")
                writer.append(t.second.toString())
            }
            writer.append("\n")
            writer.append("rhs : ")
            for (feat in example.rhsFeatures) {
                for (t in feat) {
                    writer.append(t.first.toString())
                    writer.append(":")
                    writer.append(t.second.toString())
                    writer.append("\t")
                }

            }
            writer.append("\n")
        }

        writer.flush()
    }

    companion object {
        internal var randomTL: ThreadLocal<Random> = object : ThreadLocal<Random>() {
            override fun initialValue(): Random {
                return Random(0)
            }
        }
    }
}