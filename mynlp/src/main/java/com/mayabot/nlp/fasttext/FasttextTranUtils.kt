package com.mayabot.nlp.fasttext

import com.mayabot.nlp.segment.LexerReader
import com.mayabot.nlp.segment.Lexers
import java.io.File

class FasttextTranUtils {

    companion object {

        /**
         * 处理没有分词的语料
         * __label__xxxx 语料文本,语料文本，语料文本
         */
        @JvmOverloads
        @JvmStatic
        fun prepareBySegment(from: File,
                             to: File,
                             label: String = "__label__",
                             lexer: LexerReader = Lexers.coreBuilder().build().filterReader(true, true)) {

            fun processLine(line:String): String{
                val list = ArrayList<String>()
                line.split(" ").forEach { part->
                    if(part.startsWith(label)){
                        list += part
                    }else{
                        lexer.scan(part).toWordSequence().forEach { word->
                            list += word
                        }
                    }
                }
                return list.joinToString(" ")
            }

            from.useLines { lines->
                to.bufferedWriter(Charsets.UTF_8).use { writer->
                    lines.forEach { line->
                        writer.write(processLine(line))
                        writer.write("\n")
                    }
                }
            }
        }
    }

}