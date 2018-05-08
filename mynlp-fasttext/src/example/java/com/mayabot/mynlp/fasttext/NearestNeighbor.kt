package com.mayabot.mynlp.fasttext

fun main(args: Array<String>) {

    val start = System.currentTimeMillis()
    val model = FastText.loadModel("data/fasttext/wiki.model")
    val end = System.currentTimeMillis()

    println("load wiki.model use ${end - start} ms")

    model.nearestNeighbor("中国", 10).forEach {
        println(it)
    }
    println("--------------")
    model.nearestNeighbor("孙悟空", 10).forEach {
        println(it)
    }

}