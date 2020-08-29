package com.mayabot.nlp.module

import com.mayabot.nlp.algorithm.collection.bintrie.BinTrieTreeBuilder
import java.util.function.Function

/**
 * 快速替换器：一次扫描，完成多个词的替换
 *
 * 解决的问题：假定词条数量为10万，现在需要把一篇文章里面包含这些词条的关键字全部替换为对应的超链接。
 *
 * 线程安全，可重复使用。
 *
 * @author jimichan
 */
class QuickReplacer(words: List<String>) {

    private val dict =
            BinTrieTreeBuilder.miniArray
                    .build(words.map { it to "1" }.toMap())

    fun replace(text: String, replace: (String) -> String): String {
        val matcher = dict.newForwardMatcher(text)

        // 如若没有匹配，那么直接返回text
        var m: String? = matcher.next() ?: return text

        val sb = StringBuilder()
        var point = 0

        while (m != null) {
            val offset = matcher.offset
            if (offset - point > 0) {
                sb.append(text.substring(point, offset))
            }
            val rep = replace(m)
            if (rep.isNotEmpty()) {
                sb.append(rep)
            }
            point = matcher.offset + m.length
            m = matcher.next()
        }

        if (point < text.length) {
            sb.append(text.substring(point, text.length))
        }

        return sb.toString()
    }

    fun replaceForJava(text: String, replace: Function<String, String>): String {
        val matcher = dict.newForwardMatcher(text)

        // 如若没有匹配，那么直接返回text
        var m: String? = matcher.next() ?: return text

        val sb = StringBuilder()
        var point = 0

        while (m != null) {
            val offset = matcher.offset
            if (offset - point > 0) {
                sb.append(text.substring(point, offset))
            }
            val rep = replace.apply(m)
            if (rep.length != 0) {
                sb.append(rep)
            }
            point = matcher.offset + m.length
            m = matcher.next()
        }

        if (point < text.length) {
            sb.append(text.substring(point, text.length))
        }

        return sb.toString()
    }
}

/**
 * 在内存中实现关键字高亮
 *
 * @author jimichan
 */
class Highlighter
@JvmOverloads
constructor(
        /**
         * 需要高亮的关键字
         */
        words: List<String>,
        /**
         * 高亮标签
         */
        private val tag: String = "em") {

    private val quickReplacer = QuickReplacer(words)

    fun replace(text: String): String {
        return quickReplacer.replace(text) {
            "<$tag>$it</$tag>"
        }
    }
}

/**
 * kotlin便捷函数
 */
fun String.highlight(words: List<String>, tag: String = "em"): String {
    return Highlighter(words, tag).replace(this)
}