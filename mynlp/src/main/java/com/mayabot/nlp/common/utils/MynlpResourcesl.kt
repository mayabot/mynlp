package com.mayabot.nlp.common.utils

import java.io.File

/**
 * 从maven依赖中复制文件
 */
object MynlpResources {

    @JvmStatic
    fun main(args: Array<String>) {
        install()
    }

    @JvmStatic
    fun install() {
        val dir = File(System.getProperty("user.home"), "mynlp.data")
        if (!dir.exists()) {
            dir.mkdir()
        }

        val urls = Jars().parseClassPath()
        val regex = Regex("mynlp-resource-.*\\.jar$")
        urls.filter { regex.containsMatchIn(it.file) }.forEach {
            val the = File(it.toURI())
            val out = File(dir, the.name)
            println("Copy\n${the.absolutePath}\n>>\n$out\n")
            the.copyTo(out, overwrite = true)
        }

        println("OK")

    }

}