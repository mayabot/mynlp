package com.mayabot.nlp.fasttext

import com.mayabot.nlp.blas.cosine
import java.io.File

fun main() {

//    val file = File("/Users/jimichan/Downloads/wiki.zh.bin")
//
//    val fastText = FastText.loadCppModel(file)

    val fastText = FastText.loadModel(File("/Users/jimichan/mynlp.data/wordvec.vec"), true)

    println("加载模型到内存完成")

//    val k = fastText.nearestNeighbor("丢失",5)

    fastText.like("丢", "丢失")
    fastText.like("遗落", "丢失")
    fastText.like("偷走", "丢失")
    fastText.like("遗失", "丢失")
    fastText.like("遗失", "遗落")
    fastText.like("失去", "丢失")
    fastText.like("上海", "丢失")
    fastText.like("挂失", "补办")

    println("----------------")
    fastText.senLike("卡 丢失 了", "卡 被 偷走 了")
    fastText.senLike("卡 丢失 了", "信用卡 忘记 密码 ")

//    println(fastText.analogies("柏林","德国","法国",5))

}

private fun FastText.like(word1: String, word2: String) {
    val cos = cosine(this.getWordVector(word1), this.getWordVector(word2))
    println("$word1 <-> $word2 : ${cos}")
}

private fun FastText.senLike(word1: String, word2: String) {
    val cos = cosine(this.getSentenceVector(word1.split(" ")), this.getSentenceVector(word2.split(" ")))
    println("$word1 <-> $word2 : ${cos}")
}
