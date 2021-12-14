package com.mayabot.nlp.segment.pipeline

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.segment.FluentLexerBuilder
import com.mayabot.nlp.segment.Lexer

fun lexerBuilder(blocker: FluentLexerBuilder.() -> Unit): Lexer {
    val builder = FluentLexerBuilder(Mynlp.instance())
    builder.blocker()
    return builder.build()
}