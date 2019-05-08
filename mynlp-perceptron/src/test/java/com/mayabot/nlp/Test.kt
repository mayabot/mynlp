package com.mayabot.nlp

import java.util.zip.ZipFile

fun main() {
    ZipFile("/Users/jimichan/project-new/nlp/mynlp/mynlp-perceptron/build/distributions/mynlp-perceptron-2.1.0-SNAPSHOT.zip")
            .entries().toList().forEach {
                println(it)
            }
}