package com.mayabot.nlp.segment.plugins.pos

import com.mayabot.nlp.segment.common.allFiles
import com.mayabot.nlp.segment.common.parseToFlatWords
import java.io.File

fun main() {
    genTrainData()
}
    fun genTrainData() {
        val cn = File("data.work/corpus/cncorpus")
        val pk = File("data.work/corpus/pku")

        fun read(file: File,list: MutableList<String>){
            file.allFiles().forEach { f ->
                f.forEachLine { line ->
                    if (line.isNotBlank()) {
                        val x = line.parseToFlatWords().filter { it.pos.isNotBlank() }.joinToString(separator = " ")
                        if(x.isNotBlank()) {
                                    list += x
                        }
                    }
                }
            }
        }

        val list = ArrayList<String>()

        read(cn,list)
        read(pk,list)

        list.shuffle()

        val out = File("data.work/pos.data")
        out.mkdirs()
        var k = 0
        list.asSequence().chunked(50000).forEach { part->
            k++
            File(out,"part-${k}.txt").writer(Charsets.UTF_8).use {
                part.forEach { line->
                    it.write(line)
                    it.write("\n")
                }
            }
        }

    }

