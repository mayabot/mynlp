package com.mayabot.nlp.fasttext.train

import com.mayabot.nlp.fasttext.utils.lines
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files
import java.util.regex.Pattern

class SampleLine(val words: List<String>)

class MemSampleLineList(
        private val list: MutableList<SampleLine> = ArrayList()
) : Iterable<SampleLine> {

    override fun iterator(): Iterator<SampleLine> {
        return list.iterator()
    }

    operator fun plusAssign(vo: SampleLine) {
        list += vo
    }

    operator fun plusAssign(vo: Iterable<SampleLine>) {
        list += vo
    }

}

/**
 * Iterable<TrainSample>
 */
class FileSampleLineIterable(
        val file: File
) : Iterable<SampleLine> {

    fun toMemList(): MemSampleLineList{
        return MemSampleLineList(this.toMutableList())
    }

    fun splitMutiFiles(num: Int): List<FileSampleLineIterable> {

        val subFiles = (1..num).map {
            Files.createTempFile(file.name,"").toFile()
         }

        val subFileWriter = subFiles.map { it.bufferedWriter(Charsets.UTF_8) }

        var count = 0

        file.forEachLine(charset = Charsets.UTF_8) { line ->
            subFileWriter[count % num].append(line).append("\n")
            count++
        }

        subFileWriter.forEach {
            it.flush()
            it.close()
        }

        return subFiles.map { FileSampleLineIterable(it) }.toList()
    }

    /**
     * 文件大小单位Byte
     */
    fun size(): Long {
        return file.length()
    }

    /**
     * 统计行数
     */
    fun lines(): Int {
        return file.lines().count().toInt()
    }

    override fun iterator(): Iterator<SampleLine> {
        return FileSampleIterator(file.bufferedReader(Charsets.UTF_8))
    }

    /**
     * 从文件里面迭代访问TrainSample对象
     */
    private class FileSampleIterator(val reader: BufferedReader) : AbstractIterator<SampleLine>() {

        private val pattern = Pattern.compile("\\s")

        override fun computeNext() {
            val line = reader.readLine()
            if (line == null) {
                reader.close()
                done()
            } else {
                setNext(SampleLine(line.split(pattern).filterNot { it.isBlank() }))
            }
        }

    }

}

class LoopReader(val list: Iterable<SampleLine>) : AbstractIterator<SampleLine>() {

    private var iterator = list.iterator()
    private var first = true

    override fun computeNext() {
        if (iterator.hasNext()) {
            setNext(iterator.next())
        } else {
            if (first) {
                done()
            } else {
                iterator = list.iterator()
                setNext(iterator.next())
            }
        }
        if (first) first = false
    }

}