package com.mayabot.nlp.segment.core

import com.mayabot.nlp.Mynlps
import com.mayabot.nlp.kotlin.getInstance
import com.mayabot.nlp.segment.Lexers

val coreDict = Mynlps.get().getInstance<CoreDictionary>()
val core = Lexers.core()

//fun main(){
//    println(core.scan("血吸虫").toPlainString())
//}
fun main() {
    val loadResource = Mynlps.get().env.loadResource("core-dict/CoreDict.txt")

    var total = 0
    var count = 0
    loadResource.openInputStream().bufferedReader().useLines {

        it.forEach { line ->
            total++
            val word = line.split(" ").first()
            if (word.length > 2) {
                println("$word -> ${core.scan(word).toWordList()}")
            } else if (word.length <= 2) {
                count++
            }
        }
    }
    println(count)
    println(total)
    println(total - count)
}

//长度超过3的去分词，分词器需要特殊处理，构建词图的时候和输入相等长度的词不进入词图。任何返回最优分词结果。


fun isAtom(text: String): Boolean {
    val text2 = text.toCharArray()
    val searcher = coreDict.match(text2, 0)
    var count = 0
    while (searcher.next()) {
        val len = searcher.length
        if (len == text.length || len >= 2) {

        } else {
            count++
        }
    }
    return count > 1
}

fun test(text: String): Boolean {
    val text2 = text.toCharArray()
    val searcher = coreDict.match(text2, 0)
    var count = 0
    while (searcher.next()) {
        val len = searcher.length
        if (len == text.length || len >= 2) {
        } else {
            count++
            println(String(text2, searcher.begin, searcher.length))
        }
    }
    return count <= 1
}

