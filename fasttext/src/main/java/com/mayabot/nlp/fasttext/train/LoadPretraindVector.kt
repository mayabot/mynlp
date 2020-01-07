package com.mayabot.nlp.fasttext.train

import com.mayabot.nlp.fasttext.args.Args
import com.mayabot.nlp.fasttext.blas.Matrix
import com.mayabot.nlp.fasttext.blas.floatArrayMatrix
import com.mayabot.nlp.fasttext.dictionary.Dictionary
import com.mayabot.nlp.fasttext.utils.firstLine
import java.io.File

/**
 * 加载预先训练的词向量
 * 第一行 wordNum,dim
 */
@ExperimentalUnsignedTypes
@Throws(Exception::class)
fun loadPreTrainVectors(dict: Dictionary, file: File, args: Args): Matrix {

//    var n: Int = 0
//    var dim: Int = 0
    // 第一行 wordNum,dim
    val (n, dim) = file.firstLine()!!.split(" ").map { it.toInt() }

    if (n == 0 || dim == 0) {
        throw Exception("Error format for " + file.name + ",First line must be rows and dim arg")
    }
    if (dim != args.dim) {
        throw Exception("Dimension of pretrained vectors " + dim + " does not match dimension (" + args.dim + ")")
    }

    val input = floatArrayMatrix(dict.nwords + args.bucket, args.dim)
    input.uniform(1.0f / args.dim)

    val words = ArrayList<String>(n)
    file.bufferedReader(Charsets.UTF_8).use { reader ->
        reader.readLine()//first line
        for (i in 0 until n) {
            val line = reader.readLine()
            // word 有可能是个空格，那么就不能用split了
            var parts = line.split(" ")
            if (parts.size != dim + 1) {
                if (parts.size == dim) {
//                    val sp = Splitter.on(" ").trimResults()
                    val x = line.split(" ")
                    val p = ArrayList<String>()
                    p += line.substring(0, line.indexOf(x[0]) - 1)
                    p.addAll(x)
                    parts = p
                } else {
                    throw RuntimeException("line $line parse error")
                }
            }

            val word = parts[0]

            val wordId = dict[word]
            if (wordId != -1) {
                dict.onehotMap.wordList[wordId].count++
                var x = 0
                for (j in 1..dim) {
                    input[wordId, x++] = parts[j].toFloat()
                }
            }
        }
    }

    // 这里C语言原版是threshold(1,0) 也就是说如果只是预训练向量中如果存在的x，在之前没有出现过，那么
    // 会被删除，但是在循环过程中，并没有threshold，所以会造成内存被撑大
    return input

}