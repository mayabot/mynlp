package com.mayabot.nlp.perceptron.solution

import com.google.common.base.Splitter


/**
 * 一个词项的VO
 */
class PkuWord(var word: String, var pos: String) {

    var subWord: ArrayList<PkuWord> = ArrayList()

    fun hasSub() = !subWord.isEmpty()

    override fun toString(): String {
        return if (hasSub()) {
            val sb = StringBuilder()
            sb.append("[")
            sb.append(subWord.joinToString(separator = " "))
            sb.append("]").append(pos)
            sb.toString()
        } else {
            "$word/$pos"
        }
    }

}

fun String.parseToFlatWords(): List<PkuWord> {
    return this.parseToWords().flatMap { if (it.hasSub()) it.subWord else listOf(it) }
}

//陈/nr 汝烨/nr 压题/vn 照片/n ：/w [绵阳/ns 高新技术/n 产业/n 开发区/n]nt 内/f [长虹/nz 家电城/n]ns 一角/n 。/w

val splitter = Splitter.on(" ").omitEmptyStrings().trimResults()
fun String.parseToWords(): List<PkuWord> {
    if (this.isEmpty()) {
        return listOf()
    }
    val result = ArrayList<PkuWord>()
    val words = splitter.split(this)
    var bigWord: PkuWord? = null

    for (word in words) {
        val x = word.lastIndexOf(']')
        if (word.startsWith("[") && word != "[/w") {
            bigWord = PkuWord("", "")
            val rw = word.substring(1)
            bigWord.subWord.add(rw.simpleWord())


            //开始就结束    [８６３计划/nz]nz
            if (x > 0 && x < word.length) {
                bigWord = null
                val lsp = rw.lastIndexOf("/")
                val siWord = PkuWord(rw.substring(0, lsp), word.substring(lsp + 2, x))
                val siWord2 = PkuWord(rw.substring(0, lsp), word.substring(x + 1))
                siWord2.subWord.add(siWord)
                result.add(siWord2)
            }

        } else if (bigWord != null && x > 0 && x < word.length) {
            val rw = word.substring(0, x)
            bigWord.subWord.add(rw.simpleWord())
            var ppos = word.substring(x + 1)
            if (ppos.startsWith("/")) {
                ppos = ppos.substring(1)
            }
            bigWord.pos = ppos

            result.add(bigWord)
            bigWord = null

        } else {
            if (bigWord != null) {
                bigWord.subWord.add(word.simpleWord())
            } else {
                result.add(word.simpleWord())
            }
        }
    }
    return result
}

/**
 * 压题/vn
 */
fun String.simpleWord(): PkuWord {
    val sp = this.lastIndexOf('/')
    if (sp < 0) {
        return PkuWord(this, "")
    }
    if (sp == 0) {
        return PkuWord("", this.substring(1))
    }
    return PkuWord(this.substring(0, sp), this.substring(sp + 1, this.length))
}
