package com.mayabot.nlp.module

import net.openhft.hashing.LongHashFunction


/**
 * 用来对文本进行hash
 * 第一级别：前后trim，然后hash。
 * 第二级别：去除标点、停用词然后hash。（慢一点）
 **/

object TextHash {

    private val xXHasher = LongHashFunction.xx()

    val stopwordQuickReplacer = QuickReplacer(loadStopWords())

    val l1Replace = QuickReplacer(loadL1StopWords())

    private fun loadL1StopWords(): List<String> {
        val list = ArrayList<String>()
        list += " "
        val fuhao = """
            {
            |
            }
            ~
            ¡
            ¦
            «
            ­
            ¯
            ´
            ¸
            »
            ¿
            ˇ
            ˉ
            ˊ
            ˋ
            ˜
            ‐
            —　
            ―
            ‖
            ‘
            ’
            “
            ”
            •
            …
            ‹
            ›
            ∕
            、
            。
            〈
            〉
            《
            》
            「
            」
            『
            』
            【
            】
            〔
            〕
            〖
            〗
            〝
            〞
            ︰
            ︳
            ︴
            ︵
            ︶
            ︷
            ︸
            ︹
            ︺
            ︻
            ︼
            ︽
            ︾
            ︿
            ﹀
            ﹁
            ﹂
            ﹃
            ﹄
            ﹉
            ﹊
            ﹋
            ﹌
            ﹍
            ﹎
            ﹏
            ﹐
            ﹑
            ﹔
            ﹕
            ﹖
            ﹝
            ﹞
            ﹟
            ﹠
            ﹡
            ﹢
            ﹤
            ﹦
            ﹨
            ﹩
            ﹪
            ﹫
            ！
            ＂
            ＇
            （
            ）
            ，
            ：
            ；
            ？
            ＿
            ￣
            .
            ,
            &nbsp
            &nbsp;
            --
            ?
            “
            ”
            》
        """.trimIndent()
        list += fuhao.splitToSequence("\n").filter { it.isNotBlank() }.toList()
        return list;
    }

    private fun loadStopWords(): List<String> {
        val list = ArrayList<String>()

        list += TextHash::class.java.classLoader.getResourceAsStream(
                "stopwords.txt").reader(Charsets.UTF_8).readLines().map { it.trim() }.filter {
            it.isNotEmpty()
        }

        list += " "

        return list
    }

    @JvmStatic
    fun hash1(string: String): Long {
        val text = l1Replace.replace(string, replaceFun)
        return xXHasher.hashChars(text)
    }

    private val replaceFun = { _: String -> "" }

    /**
     * 去除停用词、标点符号后，进行hash
     */
    @JvmStatic
    fun hash2(string: String): Long {
        val text = stopwordQuickReplacer.replace(string, replaceFun)
        return xXHasher.hashChars(text.trim())
    }
}

fun String.titleHash1() = TextHash.hash1(this)
fun String.titleHash2() = TextHash.hash2(this)