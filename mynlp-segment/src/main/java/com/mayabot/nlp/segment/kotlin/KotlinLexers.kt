package com.mayabot.nlp.segment.kotlin

import com.google.common.io.Files
import com.mayabot.nlp.segment.Lexers
import com.mayabot.nlp.segment.Sentence
import java.io.File

private val defaultLexer = Lexers.core()

fun String.segment(): List<String> = defaultLexer.scan(this).toWordList()
fun String.lexer(): Sentence = defaultLexer.scan(this)


/**
 * 对文本文件进行分词，输出到另外一个文件
 */
fun File.segment(outPath: String) {
    val lexerReader = defaultLexer.reader()

    val file = File(outPath)
    Files.createParentDirs(file)

    val lines = inputStream().bufferedReader().lines()

    file.outputStream().bufferedWriter().use { writer ->
        lines.filter { it.isNotBlank() }
                .map {
                    lexerReader.scan(it).toWordSequence()
                }.forEach { x ->
                    writer.write(x.joinToString(separator = " "))
                    writer.newLine()
                }
    }
}

fun main() {
    println("华为海思总裁致信".lexer())
}