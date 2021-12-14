package com.mayabot.nlp.module.nwd

import java.io.File
import java.nio.charset.Charset

//fun main() {
//    val x = FilesNewWordFind(File("data/source/test/newword/data-shanghai"))
//
//    x.runSaveToFile(File("data/output/test/newword/data-shanghai-out.csv"))
//}

/**
 * 扫描指定文件夹下面的所有文本文件，计算发现的新词
 *
 */
class FilesNewWordFind(
    private val dir: java.io.File,
    private val charset: Charset = Charset.forName("UTF-8"),
    private val minGroup: Int = 3,
    private val maxGroup: Int = 12,
        private val minOccurCount: Int = 10,
        private val minMi: Float = 2.0f,
        private val minEntropy: Float = 1.0f,
        private val excludeCoreDict: Boolean = true) {

    var verbose = true

    fun runSaveToFile(file: File, head: Boolean = true, charset: Charset = Charset.forName("GBK")) {
        val result = run()

        val out = file.bufferedWriter(charset)
        out.use {
            if (head) {
                it.write("Word,WordLen,Score,Freq,DocFreq,Freq/DocFreq,Idf,Mi,AvgMi,Entropy,le,re")
                it.newLine()
            }
            result.forEach {
                out.write("${it.word},${it.len},${String.format("%.2f", it.score)},${it.freq},${it.docFreq},${it.freq.toFloat() / it.docFreq},${it.idf},${it.mi},${it.avg_mi},${it.entropy},${it.le},${it.re}")
                out.newLine()
            }
        }
    }

    fun run(): ArrayList<NewWord> {
        val engine = NewWordFindEngine(minGroup, maxGroup, minOccurCount, excludeCoreDict)

        val files = dir.listFiles().filter { it.isFile }

        val fileCount = files.size

        if (verbose) println("第一轮扫描")
        var ac = 0
        val t1 = System.currentTimeMillis()
        if (verbose) print("0%")
//        val splitter = Splitter.on("\n").omitEmptyStrings().trimResults()
        files.forEach { file ->
            ac++
            if (verbose) print("\r${((ac * 100.0f) / fileCount).toInt()}%")

            file.bufferedReader(charset).use { reader ->
                reader.lines().forEach { document ->
                    engine.firstScan(document)
                }
            }
        }

        val t2 = System.currentTimeMillis()
        if (verbose) println("第一轮扫描耗时 ${t2 - t1} ms")

        engine.finishFirst()

        println()
        if (verbose) println("第二轮扫描")
        val t3 = System.currentTimeMillis()
        ac = 0
        if (verbose) print("0%")
        files.forEach { file ->
            ac++
            print("\r${((ac * 100.0f) / fileCount).toInt()}%")
            file.bufferedReader(charset).use { reader ->
                reader.lines().forEach { document ->
                    engine.secondScan(document)
                }
            }
        }
        engine.endSecond()
        val t4 = System.currentTimeMillis()
        if (verbose) println("第二轮扫描耗时 ${t4 - t3} ms")

        return engine.result(minMi, minEntropy)
    }

}