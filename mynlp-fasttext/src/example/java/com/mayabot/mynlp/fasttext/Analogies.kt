package com.mayabot.mynlp.fasttext


fun main(args: Array<String>) {

    val start = System.currentTimeMillis()
    val model = FastText.loadModel("data/fasttext/wiki.model")
    val end = System.currentTimeMillis()

    println("load wiki.model use ${end - start} ms")

    model.analogies("国王","皇后","男",5).filterNot { it.first<0.7f }.forEach { println(it) }

}