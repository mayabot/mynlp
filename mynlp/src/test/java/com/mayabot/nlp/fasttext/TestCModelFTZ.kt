package com.mayabot.nlp.fasttext

import java.io.File

fun main() {
    val file = File("/Users/jimichan/Downloads/ChineseJapaneseKoreanLangIder.ftz")

    val model = FastText.loadCppModel(file)

    val x = model.predict(listOf("hello", "hi"), 1, 0.0f)
    println(x)
}