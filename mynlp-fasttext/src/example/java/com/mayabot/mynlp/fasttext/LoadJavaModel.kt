package com.mayabot.mynlp.fasttext

import java.io.DataInputStream
import java.io.File
import java.nio.ByteOrder

fun main(args: Array<String>) {


    val fastText = FastText.loadModel("data/fasttext/javamodel")
    AgnewsTest.predict(fastText)




}

