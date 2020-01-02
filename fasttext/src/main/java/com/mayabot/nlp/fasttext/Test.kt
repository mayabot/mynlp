package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.args.ModelArgs
import java.io.File

fun main() {
    val args = ModelArgs()
    val file = File("fasttext/data/model.bin")

    CppFastTextSupport.load(file)
}