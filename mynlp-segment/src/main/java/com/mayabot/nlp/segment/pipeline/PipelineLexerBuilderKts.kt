package com.mayabot.nlp.segment.pipeline

import com.mayabot.nlp.segment.core.CoreLexerPlugin
import com.mayabot.nlp.segment.plugins.Plugins

fun PipelineLexerBuilder.dsl(blocker: PipelineLexerBuilderDsl.() -> Unit) {
    val dsl = PipelineLexerBuilderDsl(this)
    dsl.blocker()
}

fun pipelineLexerBuilder(blocker: PipelineLexerBuilderDsl.() -> Unit): PipelineLexerBuilder {
    val builder = PipelineLexerBuilder.builder()
    builder.dsl(blocker)
    return builder
}

class PipelineLexerBuilderDsl(val builder: PipelineLexerBuilder) {
    fun installCore() {
        builder.install(CoreLexerPlugin())
    }

    fun installPos() {
        builder.install(Plugins.posPlugin())
    }

    fun installCorrection() {
        builder.install(Plugins.correctionPlugin())
    }

    fun installCustomDictionaryPlugin() {
        builder.install(Plugins.customDictionaryPlugin())
    }

    fun installNerPlugin() {
        builder.install(Plugins.nerPlugin())
    }

    fun installPersonNamePlugin() {
        builder.install(Plugins.personNamePlugin())
    }
}

fun main() {
    val builder = pipelineLexerBuilder {
        installCore()
        installPos()
    }
    val lexer = builder.build()

    println(lexer.scan("你好中文世界"))
}