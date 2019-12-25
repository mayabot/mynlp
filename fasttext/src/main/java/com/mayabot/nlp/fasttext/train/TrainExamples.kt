package com.mayabot.nlp.fasttext.train

import com.google.common.base.Splitter
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException

class TrainSample(val words: List<String>)

/**
 * Iterable<TrainSample>
 */
class TrainSampleList(val file: File) : Iterable<TrainSample> {
    override fun iterator(): Iterator<TrainSample> {
        return FileSampleIterator(file.bufferedReader(Charsets.UTF_8))
    }
}

/**
 * 从文件里面迭代访问TrainSample对象
 */
class FileSampleIterator(val reader: BufferedReader) : AbstractIterator<TrainSample>() {

    override fun computeNext() {
        val line = reader.readLine()
        if (line == null) {
            reader.close()
            done()
        } else {
            setNext(TrainSample(line.split(" ").filterNot { it.isBlank() }))
        }
    }

    fun close(){
        reader.close()
    }

}

/**
 * 把原始语料分词，并切分成多个文件
 */
fun processAndSplit(source: File, wordSplitter: WordSplitter, num: Int): List<TrainSampleList> {
    if (!source.exists()) {
        throw FileNotFoundException(source.absolutePath)
    }

    val dir = source.parentFile
    val fileName = source.name

    val subFiles = (1..num).map { File(dir, fileName + "_" + it) }

    val subFileWriter = subFiles.map { it.bufferedWriter(Charsets.UTF_8) }

    var count = 0
    source.forEachLine(charset = Charsets.UTF_8) { line ->
        val toLine = if (wordSplitter == whitespaceSplitter) {
            line+"\n"
        } else {
            wordSplitter.split(line).joinToString(separator = " ", postfix = "\n")
        }
        subFileWriter[count % num].append(
                toLine
        )
        count++
    }

    subFileWriter.forEach {
        it.flush()
        it.close()
    }

    return subFiles.map { TrainSampleList(it) }.toList()
}

/**
 *  如果分词器不能把 __lable__xxxx 分为一个词，那么要特殊处理一下
 */
interface WordSplitter {
    fun split(text: String): List<String>
}

/**
 * 默认就是这么实现的
 */
object whitespaceSplitter : WordSplitter {

    val whitespace = Splitter.on(' ').omitEmptyStrings()

    override fun split(text: String): List<String> {
        return whitespace.splitToList(text)
    }
}


class LoopReader ( val list: TrainSampleList) : AbstractIterator<TrainSample>() {
    var iterator = list.iterator()
    var first = true
    override fun computeNext() {
        if (iterator.hasNext()) {
            setNext(iterator.next())
        }else{
            if(first){
                done()
            }else{
                iterator = list.iterator()
                setNext(iterator.next())
            }
        }
        if(first) first = false
    }

    fun close(){
        val it = iterator
        if (it is FileSampleIterator) {
            it.close()
        }
    }
}


///**
// * 基于内存的实现。提供add的方法，全部在内存里面。
// */
//class MemTrainExampleSource(val splitter: WordSplitter) : TrainExampleSource{
//
//    var list:MutableList<List<String>> = Lists.newArrayList<List<String>>()
//
//    fun addExample(text: String) {
//        list.add(splitter.split(text))
//    }
//
//    fun addExample(text: String,label:String) {
//        var split = ArrayList<String>().apply {
//            addAll(splitter.split(text))
//            add(label)
//        }
//        list.add(split)
//    }
//
//    override fun iteratorAll(): ExampleIterator {
//        var iterator = list.iterator()
//        return object : ExampleIterator {
//            override fun close() {
//            }
//            override fun hasNext(): Boolean {
//                return iterator.hasNext()
//            }
//            override fun next(): List<String> {
//                return iterator.next()
//            }
//        }
//    }
//
//    override fun split(num: Int): List<TrainExampleSource> {
//        return Lists.partition(list,num.toInt()).map {
//            val x = MemTrainExampleSource(splitter)
//            x.list = it
//            x
//        }.toList()
//
//    }
//
//    override fun clean() {
//
//    }
//}