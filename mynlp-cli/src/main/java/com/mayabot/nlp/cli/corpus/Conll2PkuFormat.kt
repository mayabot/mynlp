package com.mayabot.nlp.cli.corpus

import com.google.common.base.Splitter
import java.io.File


/**
 * 提取Conll的格式为PKU语料库格式
 */
object Conll2PkuFormat {
    @JvmStatic
    fun main(args: Array<String>) {
        parse(File("/Users/jimichan/Downloads/conll"), File("data/corpus/conll/conll_2012.txt"))
    }


    fun parse(from: File, to: File) {

        var size = 0
        from.walkBottomUp().filter { !it.name.startsWith(".") && it.isFile && it.name.endsWith("_gold_conll") }
                .forEach {
                    var text = it.readText()
                    var result = parse1(text) + "\n"

                    to.appendText(result)
                }
        println(size)
    }

    val splitter = Splitter.on(" ").omitEmptyStrings().trimResults()

    fun parse1(text: String): String {
        val out = StringBuilder()
        //var sentence = ArrayList
        var nerStatus: String? = null
        for (line in text.lines().filter { !it.startsWith("#") }) {
            val row = splitter.splitToList(line)

            if (row.isEmpty()) {
                out.append("\n")
            } else {
                val word = row[3]
                val pos = row[4]
                val ner = row[10]
                if (ner.startsWith("(") && ner.endsWith("*")) {
                    nerStatus = ner.substring(1, ner.length - 1)
                    out.append("[")
                }

                out.append(word).append("/").append(mapPos(pos.toLowerCase()))

                if (nerStatus != null && ner == "*)") {
                    out.append("]/" + mapNER(nerStatus.toLowerCase()))
                    nerStatus = null
                }
                out.append(" ")
            }
        }

        return out.toString()
    }
    //data class Word(val word:String,val pos:String)

    fun mapPos(pos: String): String {
        return when (pos) {
            "pu" -> "w"
            else -> pos
        }
    }

    fun mapNER(ner: String): String {
        return when (ner) {
            "org" -> "nt"
            "person" -> "nr"
            "date" -> "t"
            else -> ner
        }
    }
}
