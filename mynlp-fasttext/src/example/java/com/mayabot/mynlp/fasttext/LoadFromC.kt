package com.mayabot.mynlp.fasttext

fun main(args: Array<String>) {
    val train = FastText.loadFasttextBinModel("data/fasttext/model.bin")
    AgnewsTest.predict(train)
}
