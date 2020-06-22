package com.mayabot.nlp.fasttext

import java.io.File

fun main() {
    val model = FastText.loadCppModel(File("fastText4j/data/ChineseJapaneseKoreanLangIder.ftz"))

    val list = model.predict(listOf("こんにちは"), 3, 0.1f)

    list.forEach {
        println(it)
    }

}