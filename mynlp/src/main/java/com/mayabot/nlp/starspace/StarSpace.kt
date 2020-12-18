@file:Suppress("NAME_SHADOWING")

package com.mayabot.nlp.starspace

import com.mayabot.nlp.blas.ByteBufferMatrix
import com.mayabot.nlp.blas.DenseMatrix
import com.mayabot.nlp.blas.Vector
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


/**
 * StartSpace
 */
class StarSpace(
    val dict: Dictionary,
    val args: Args,
    lhsMatrix: DenseMatrix,
    rhsMatrix: DenseMatrix
) {

    private val lhsEmbeddings = SparseLinear(lhsMatrix)

    private val rhsEmbeddings = SparseLinear(rhsMatrix)

    fun projectLHS(ws: List<XPair>): Vector {
        val vec = lhsEmbeddings.forward(ws)

        if (ws.isNotEmpty()) {
            val norm = args.norm2Computer(ws, vec)
            vec /= (norm)
        }

        return vec
    }

    fun projectRHS(ws: List<XPair>): Vector {
        val vec = rhsEmbeddings.forward(ws)
        if (ws.isNotEmpty()) {
            val norm = args.norm2Computer(ws, vec)
            vec /= (norm)
        }
        return vec
    }

    fun findLHSLike(point: Vector, numSim: Int): List<XPair> {
        var numSim = numSim
        if (numSim <= 0) {
            numSim = 5
        }
        return kNN(lhsEmbeddings, point, numSim)
    }

    fun findRHSLike(point: Vector, numSim: Int): List<XPair> {
        var numSim = numSim
        if (numSim <= 0) {
            numSim = 5
        }
        return kNN(rhsEmbeddings, point, numSim)
    }


    //邻近算法，或者说K最近邻(kNN，k-NearestNeighbor)分类算法是数据挖掘分类技术中最简单的方法之一

    /**
     *
     * @param lookup
     * @param point
     * @param numSim
     * @return
     */
    fun kNN(lookup: SparseLinear, point: Vector, numSim: Int): List<XPair> {

        //        typedef pair<int32_t, Real> Cand;
        val maxn = dict.nwords() + dict.nlabels()


        val mostSimilar = (0 until Math.min(numSim, maxn)).map { XPair(-1, -1.0f) }.toTypedArray()
        val mastSimilarLast = mostSimilar.size - 1
        for (i in 0 until maxn) {
            val contV = lookup.forward(i)
            val sim = args.similarity(point, contV)

            if (sim > mostSimilar[mastSimilarLast].second) {
                val last = mostSimilar[mastSimilarLast]
                last.first = i
                last.second = sim
                mostSimilar.sortByDescending { it.second }
            }
        }

        val result = ArrayList<XPair>()
        for (r in mostSimilar) {
            if (r.first == -1 || r.second.toDouble() == -1.0) {
            } else {
                result.add(r)
            }
        }
        return result
    }

    /**
     * 一个文档的向量
     * @param line
     * @return
     */
    fun getDocVector(line: String): Vector {
        val pairs = dict.parseDoc(line)
        return projectLHS(pairs)
    }

    fun prediction(docBaseFile: String?): StarSpacePrediction {
        return StarSpacePrediction(this, docBaseFile)
    }

    fun prediction(): StarSpacePrediction {
        return prediction(null)
    }

    fun nearestNeighbor(line: String, k: Int): List<QueryEntity> {
        val docVector = getDocVector(line)
        var lhsLike = findLHSLike(docVector, k)
        return lhsLike.map { QueryEntity(it.first, dict.getSymbol(it.first), it.second) }.toList()
    }

    fun loadBaseDocs(basedoc: String? = null): BaseDocs {
        val result = BaseDocs()
        if (basedoc.isNullOrBlank()) {
            if (args.fileFormat == FileFormat.LabelDoc) {
                System.err.println("Must provide base labels when label is featured.\n")
                exitProcess(0)
            }

            for (i in 0 until dict.nlabels()) {
                result.baseDocs.add(listOf(XPair(i + dict.nwords(), 1.0f)))
                result.baseDocVectors.add(
                    this.projectRHS(listOf(XPair(i + dict.nwords(), 1.0f)))
                )
            }

            println("Prediction use  ${dict.nlabels()} known labels.")
        } else {
            println("Loading base docs from file : $basedoc")
            File(basedoc).useLines { lines ->

                lines.forEach { line ->
                    val ids = dict.parseDoc(line)
                    result.baseDocs.add(ids)
                    val docVec = this.projectRHS(ids)
                    result.baseDocVectors.add(docVec)
                }
            }
            if (result.baseDocVectors.size == 0) {
                throw RuntimeException("ERROR: basedocFile file '$basedoc' is empty.")
            }
            println("Finished loading ${result.baseDocVectors.size} base docs.\n")
        }
        return result
    }

    fun similarity(a: Vector, b: Vector): Float = args.similarity(a, b)

    /**
     * 评估TEST模型RANK
     */
    fun evaluate(testFile: String, k: Int = 5, basedocFile: String? = null, predictionFile: String? = null) {
        Evaluate(this, testFile, k, basedocFile, predictionFile).evaluate()
    }

    /**
     * 保存模型到位tsv模型
     */
    fun saveTsv(file: String) {
        var file = file

        if (!file.endsWith(".tsv")) {
            file += ".tsv"
        }

        println("Start save tsv file $file")

        // LHS，只保存了
        val matrix = lhsEmbeddings.matrix
        val cols = matrix.col

        File(file).bufferedWriter().use { writer ->
            val size = dict.nwords() + dict.nlabels()

            for (i in 0 until size) {
                val symbol = dict.getSymbol(i)
                writer.append(symbol)

                val row = matrix[i]

                val sb = StringBuilder()
                for (j in 0 until cols) {
                    sb.append(String.format("\t%.8f", row[j]))
                }
                sb.append("\n")
                writer.write(sb.toString())
            }
        }

    }

    /** 保存到二进制模型
     */
    fun saveModel(file: String) {


        var file = file

        if (!file.endsWith("/")) {
            file += "/"
        }

        if (!File(file).exists()) {
            File(file).mkdir()
            File(file).mkdirs()
        }

        // save args
        Files.newOutputStream(Paths.get(file, "arg.bin")).use {
            this.args.write(it)
            it.flush()
        }

        // save dict
        Files.newOutputStream(Paths.get(file, "dict.bin")).use {
            BufferedOutputStream(it).apply {
                dict.save(this)
            }.flush()
        }

        // save lsh
        RandomAccessFile(Paths.get(file, "lsh.matrix").toFile(), "rw")
            .use { file ->
                file.channel.use { channel ->
                    lhsEmbeddings.matrix.save(channel)
                }
            }


        // save rsh
        if (!args.shareEmb) {
            RandomAccessFile(Paths.get(file, "rsh.matrix").toFile(), "rw")
                .use { file ->
                    file.channel.use { channel ->
                        rhsEmbeddings.matrix.save(channel)
                    }
                }
        }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun loadTsv(file: String, args: Args = Args()): StarSpace {
            System.out.println("Start to load a trained embedding model in tsv format.\n")

            val first = File(file).firstLine()!!
            val dim = first.split("\t").size - 1
            println("Setting dim from Tsv file to: $dim")

            args.dim = dim

            val dictionary = Dictionary(args)
            dictionary.loadDictFromTsvModel(file)

            val matrix = ByteBufferMatrix(dictionary.nwords() + dictionary.nlabels(), args.dim, direct = true)

            val starSpace = StarSpace(dictionary, args, matrix, matrix)

            val cols = args.dim

            var lineNum = 0

            File(file).useLines { lines ->
                lines.forEach { line ->
                    lineNum++

                    val pieces = line.split("\t")

                    if (pieces.size != cols + 1) {
                        System.err.println("error line $line")
                        return@forEach
                    }

                    val idx = dictionary.getId(pieces[0])
                    if (idx == -1) {
                        System.err.println("Failed to insert record: $line")
                        return@forEach
                    }

                    for (i in 0 until cols) {
                        val value = pieces[i + 1].toFloat()
                        matrix[idx, i] = value
                    }
                }
            }

            return starSpace
        }

        @JvmStatic
        @JvmOverloads
        fun loadModel(file: String, mmap: Boolean = false): StarSpace {

            var file = file

            val t1 = System.currentTimeMillis()

            if (!file.endsWith("/")) {
                file += "/"
            }

            // read args
            val args = Files.newInputStream(Paths.get(file, "arg.bin")).use {
                Args().apply { read(it) }
            }

            //read dict
            val dict = Files.newInputStream(Paths.get(file, "dict.bin")).use {
                BufferedInputStream(it).let {
                    Dictionary(args).load(it)
                }
            }

            var numLhs = dict.nwords() + dict.nlabels()
            if (args.ngrams > 1) {
                numLhs += args.bucket
            }


            val lshMatrix = RandomAccessFile(Paths.get(file, "lsh.matrix").toFile(), "r")
                .use { file ->
                    file.channel.use { channel ->
                        val byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                        if (mmap) {
                            byteBuffer
                        } else {
                            ByteBuffer.allocateDirect(channel.size().toInt()).apply {
                                this.put(byteBuffer)
                            }
                        }
                    }
                }

            val rshMatrix = if (args.shareEmb) {
                lshMatrix
            } else {
                RandomAccessFile(Paths.get(file, "rsh.matrix").toFile(), "r")
                    .use { file ->
                        file.channel.use { channel ->
                            val byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                            if (mmap) {
                                byteBuffer
                            } else {
                                ByteBuffer.allocateDirect(channel.size().toInt()).apply {
                                    this.put(byteBuffer)
                                }
                            }
                        }
                    }
            }

            val t2 = System.currentTimeMillis()

            println("Load model use time ${t2 - t1}")
            return StarSpace(
                dict, args,
                ByteBufferMatrix(numLhs, args.dim, lshMatrix),
                ByteBufferMatrix(numLhs, args.dim, rshMatrix)
            )
        }
    }

}

data class BaseDocs(
    val baseDocVectors: MutableList<Vector> = ArrayList(),
    var baseDocs: MutableList<List<XPair>> = ArrayList()
)

data class XPair(@field:JvmField var first: Int, @field:JvmField var second: Float) {

    companion object {
        @JvmStatic
        fun createPair(first: Int, second: Float): XPair {
            return XPair(first, second)
        }

        @JvmStatic
        fun createPair(first: Long, second: Float): XPair {
            return XPair(first.toInt(), second)
        }
    }
}

data class QueryEntity(
    @field:JvmField var idx: Int,
    @field:JvmField var entry: String,
    @field:JvmField var score: Float
)