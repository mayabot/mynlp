package com.mayabot.nlp.fasttext

import java.io.File

fun main() {

//    val file = File("/Users/jimichan/Downloads/wiki.zh.bin")
//
//    val fastText = FastText.loadCppModel(file)

    val fastText = FastText.loadModel(File("/Users/jimichan/Downloads/wiki.fasttext"),true)

    val k = fastText.nearestNeighbor("上海",5)

    println(k)
    println(fastText.analogies("柏林","德国","法国",5))

}