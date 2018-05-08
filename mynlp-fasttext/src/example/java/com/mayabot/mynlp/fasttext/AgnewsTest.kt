package com.mayabot.mynlp.fasttext

import com.google.common.base.CharMatcher
import com.google.common.base.Charsets
import com.google.common.base.Splitter
import com.google.common.io.Files
import java.io.File


//C 语言的训练结果
//Read 5M words
//Number of words:  95811
//Number of labels: 4
//Progress: 100.0% words/sec/thread: 2708683 lr:  0.000000 loss:  0.268832 ETA:   0h 0m

//Read 5M words
//Number of words:  95811
//Number of labels: 4
//Progress: 100.00% words/sec/thread:  5673678 lr: 0.00000 loss: 0.28372 ETA: 0h 0m 0s
//Train use time 5093 ms
//total=7600
//right=6936
//rate 0.9126315789473685
//
//Process finished with exit code 0

fun main(args: Array<String>) {
    val file = File("data/fasttext/ag.train")

    val train = FastText.train(file, ModelName.sup)

    AgnewsTest.predict(train)
}

object AgnewsTest{

    @Throws(Exception::class)
    fun predict(fastText: FastText) {

        var total = 0
        var right = 0
        val splitter = Splitter.on(CharMatcher.whitespace())

        for (line in Files.asCharSource(File("data/fasttext/ag.test"), Charsets.UTF_8).readLines()) {

            val i = line.indexOf(',')
            val label = line.substring(0, i).trim { it <= ' ' }
            val text = line.substring(i + 1)
            total++

            val predict = fastText.predict(splitter.split(text), 3)

            if (!predict.isEmpty()) {
                if (label == predict.get(0).second) {
                    right++
                }
            }
        }


        println("total=$total")
        println("right=$right")
        println("rate " + right * 1.0 / total)

        checkArgument(right > 0.9f)

    }
}
