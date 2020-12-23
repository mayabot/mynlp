package com.mayabot.nlp.segment

import com.mayabot.nlp.Mynlp
import java.io.File

private val defaultLexer = Mynlp.instance().lexerBuilder()
    .bigram().withPersonName().build()

fun String.segment(): List<String> = defaultLexer.scan(this).toWordList()
fun String.lexer(): Sentence = defaultLexer.scan(this)


/**
 */
fun File.segment(outPath: String) {
    val lexerReader = defaultLexer.reader()

    val file = File(outPath)

    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }

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
