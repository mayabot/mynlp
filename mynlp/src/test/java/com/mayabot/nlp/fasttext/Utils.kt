package com.mayabot.nlp.fasttext

import com.mayabot.nlp.fasttext.train.MemSampleLineList
import com.mayabot.nlp.fasttext.train.SampleLine

fun loadTrainFile( resouceName:String ) : MemSampleLineList{

    val path = "/"+resouceName

    val ins = TestSup::class.java.getResourceAsStream(path)

    val list = ArrayList<String>()
    ins.bufferedReader().lines().forEach {
        list += it
    }

    val x = list.map { SampleLine(it.split(" ").toList()) }.toMutableList()

    return MemSampleLineList(x)

}