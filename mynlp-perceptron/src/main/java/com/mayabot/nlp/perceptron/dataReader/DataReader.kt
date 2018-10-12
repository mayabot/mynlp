package com.mayabot.nlp.perceptron.dataReader

import com.mayabot.nlp.perceptron.SequenceLabel
import com.mayabot.nlp.perceptron.lmpl.CharNorm
import com.mayabot.nlp.perceptron.lmpl.listTOSequence
import com.mayabot.nlp.perceptron.lmpl.pkuSplitter
import java.io.File
import kotlin.collections.ArrayList

class ConllFileReader(){
    fun conllFileReader(directory: String): MutableIterable<SequenceLabel<Char>> {
        val list = ArrayList<SequenceLabel<Char>>()
        val root = File(directory)
        root.walk().forEach { file ->
            if (file.name.contains("gold_conll")) {
                val sentence = ArrayList<String>()
                file.bufferedReader().lineSequence().drop(1).forEach {
                    try {
                        if (it != "") {
                            val words = it.split("\\s+".toRegex())[3]
                            sentence.add(CharNorm.convert(words))
                        } else {
                            list.add(listTOSequence(sentence))
                            sentence.clear()
                        }
                    } catch (e: Exception) {
                        if (it.indexOf("#") != 0)
                            println(e)
                    }
                }
            }
        }
        return list
    }


}

class PkuFileReader(private val directory: String){

    fun readWord(): MutableIterable<SequenceLabel<Char>> {
        val list = ArrayList<SequenceLabel<Char>>()
        val root = File(directory)
        root.bufferedReader().lineSequence().forEach {
            if (it != "") {
                val t = listTOSequence(pkuSplitter(it))
                list.add(t)
            }
        }
        return list
    }

    fun readPosTag(tagSet : ArrayList<String>):MutableIterable<SequenceLabel<String>>{
        val list = ArrayList<SequenceLabel<String>>()
        val root = File(directory)
        root.bufferedReader().lineSequence().forEach {
            if (it != "") {
                val t = dataExtractor(it,tagSet)
                list.add(t)
            }
        }
        return list
    }

    private fun dataExtractor(sentence: String, tagSet : ArrayList<String>): SequenceLabel<String> {
//        val normedSentence = CharNorm.convert(sentence)
        val sequence = ArrayList<String>()
        val labelSet = ArrayList<Int>()

        val temp = sentence.split("\\s+".toRegex())
        for (i in 1 until temp.size) {
            val t = temp[i].split("/")
            var word = t[0]
            try {
                var label = t[1]
                if (word == "") continue
                if (t.indexOf("[") != -1) word = word.substring(1)
                if (label.contains("]")) label = label.substring(0, label.indexOf("]"))
                sequence.add(CharNorm.convert(word))
                labelSet.add(idOf(label, tagSet))
            }catch (e: Exception){
                t
            }

        }
        return SequenceLabel(sequence.toTypedArray(),labelSet.toIntArray())
    }

    private fun idOf(tag: String,tagSet : ArrayList<String>): Int{
        if (tagSet.contains(tag)) return tagSet.indexOf(tag)
        tagSet.add(tag)
        return tagSet.size - 1
    }
}