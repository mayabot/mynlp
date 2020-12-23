package com.mayabot.nlp.cli

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.common.logging.InternalLogLevel
import com.mayabot.nlp.common.logging.JdkLogger
import com.mayabot.nlp.segment.segment

fun main(args: Array<String>) {

    JdkLogger.defaultLevel = InternalLogLevel.WARN;

    Mynlp.configer().setAutoDownloadRes(true)

    println("2012年的冬天".segment())

    if (args.isEmpty()) {
        printTopHelp()
        return
    }
    val subcommand = args.first()
    val commandArgs = args.drop(1).toTypedArray()

}

fun printTopHelp() {
    println(
        """
        Usage: mynlp subcommand [OPTION]...
        
        Mynlp实用工具,提供多个subcommand执行不同的功能.
        
        Subcommand List:
        
        segment     中文分词
        ner         命名实体
        pos         词性分析
        name        人名模型
        perceptron  通用AP训练和评估
        train       内部模型训练入口
        nwd         新词发现
        fastText    分类模型和词嵌入
        t2s         繁简体转换
        pinyin      文字转拼音
        pinyin-split    拼音流切分（nihaoshijie --> ni hao shi jie）
        hash
        classify    便捷的文本分类
        
        
    """.trimIndent()
    )
}