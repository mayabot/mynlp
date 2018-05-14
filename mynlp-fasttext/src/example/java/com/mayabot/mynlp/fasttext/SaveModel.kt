package com.mayabot.mynlp.fasttext

/**
 *
 */
fun main(args: Array<String>) {

    val fastText = FastText.loadFasttextBinModel("data/fasttext/wiki.zh.bin")

    println("loaded")


    fastText.saveModel("data/fasttext/wiki.model")


}

