package com.mayabot.nlp.perceptron.model

import java.io.File

fun main(args: Array<String>) {
    norm()
}

fun norm(){
    val normTable = HashMap<Char, Char>()
    var count = 0
    File("/Users/mei_chaofeng/workspace/mynlp/mynlp-perceptron/src/main/java/com/mayabot/nlp/perceptron/norm")
            .bufferedReader()
            .use { bufferedReader ->
        bufferedReader.lineSequence().forEach {
            count ++
            try {

                normTable[it[0]] = it[2]

            }catch (e :Exception){
                println(it)
                println(count)
            }
        }
    }
}