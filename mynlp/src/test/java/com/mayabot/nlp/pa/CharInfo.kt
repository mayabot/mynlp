package com.mayabot.nlp.pa

import com.mayabot.nlp.character.ChineseCharInfos

class CharInfo {

}

fun main() {

    val sChars = ChineseCharInfos.array.mapIndexedNotNull{ index, chineseCharInfo ->
        if(chineseCharInfo.struct<0){
            null
        }else{
            index.toChar()
        }
    }.toSet()

    val pinyinChars = ChineseCharInfos.array.mapIndexedNotNull{ index, chineseCharInfo ->
        if(chineseCharInfo.pinyin == null){
            null
        }else{
            index.toChar()
        }
    }.toSet()

    val biChars = ChineseCharInfos.array.mapIndexedNotNull{ index, chineseCharInfo ->
        if(chineseCharInfo.writeNum < 0){
            null
        }else{
            index.toChar()
        }
    }.toSet()

    println("有拼音，但是没有结构")
    pinyinChars.forEach {
        if(it !in sChars){
            println(it)
        }
    }

    println("有结构，但是没有拼音")
    sChars.forEach {
        if(it !in pinyinChars){
            println(it)
        }
    }
}