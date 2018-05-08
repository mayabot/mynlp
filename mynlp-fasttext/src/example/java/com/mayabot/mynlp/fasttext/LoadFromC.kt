package com.mayabot.mynlp.fasttext

fun main(args: Array<String>) {
    val train = FastText.loadCModel("data/fasttext/model.bin")
    AgnewsTest.predict(train)
}
