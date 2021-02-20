package com.mayabot.nlp.character

import com.mayabot.nlp.module.pinyin.model.Pinyin

/**
 * 新四角编码
 */
object ChineseCharInfos {

    val array : Array<ChineseCharInfo>

    init {
        val codeArray = loadFromFile(fourCode)
        val structArray = loadFromFile(charStrut)
        val writeNumArray = loadFromFile(writeNum)

        val pinyin = loadPinyin()

        this.array = Array(65535) { i ->
            val code = codeArray[i]
            val codeString = if(code == -1){
                ""
            }else{
                code.toString().padStart(5,'0')
            }

            ChineseCharInfo(
                codeString,
                structArray[i],
                writeNumArray[i],
                pinyin[i]
            )
        }

    }

    operator fun get(i:Int) = array[i]

    operator fun get(i:Char) = array[i.toInt()]
}

/**
 * 新四角码
 */
const val fourCode = "/mynlp/char_four_code.txt"

/**
 * 字体结构
 */
const val charStrut = "/mynlp/char_struct.txt"

/**
 * 笔画数
 */
const val writeNum = "/mynlp/char_write_num.txt"

private fun loadFromFile(name: String):IntArray{
    val array = IntArray(65535){-1}
    ChineseCharInfos::class.java.getResourceAsStream(name)
        .bufferedReader(Charsets.UTF_8)
        .useLines { lines ->
            lines.forEach { line ->
                val (zi, code) = line.split(" ")
                array[zi[0].toInt()] = code.toInt()
            }
        }

    return array
}

private fun loadPinyin(): Array<Array<Pinyin>?> {
    val array = Array<Array<Pinyin>?>(65535){null}
    ChineseCharInfos::class.java.getResourceAsStream("/mynlp/char_py.txt")
        .bufferedReader(Charsets.UTF_8)
        .useLines { lines ->
            lines.forEach { line ->
                val (left, right) = line.split("=")
                val pys = right.split(",").map { Pinyin.valueOf(it) }.toTypedArray()
                array[left[0].toInt()] = pys
            }
        }
    return array
}

data class ChineseCharInfo(
    /**
     * 5位四角码
     * 不存在的话，使用空字符
     */
    val codeString:String,
    /**
     * 结构
     * 0 单子 一
     * 1 左右结构 体
     * 2 上下结构
     * 3 左中右
     * 4 上中下
     * 5 右上包围 氕 氖
     * 6 左上包围 暦
     * 7 左下包围 暹
     * 8 上三包围
     * 9 下三包围
     * 10 左三包围
     * 11 全包围
     * 12 镶嵌
     * 13 品字
     * 不存在的话等于-1
     * key_word_lst = ['danyi', 'zuoyou', 'shangxia', 'zuozhongyou', 'shangzhongxia', 'youshangbaowei', 'zuoshangbaowei','zuoxiabaowei','shangsanbaowei','xiasanbaowei','zuosanbaowei','quanbaowei','xiangqian','pinzi']
     */
    val struct:Int,
    /**
     * 笔画
     * 不存在的话，等于-1
     */
    val writeNum:Int,
    val pinyin:Array<Pinyin>?
){
    companion object{
        val char2ByteMap = ByteArray(128).apply {
            for(i in '0' .. '9'){
                this[i.toInt()] = i.toByte()
            }
        }
    }

    /**
     * 访问四角码
     * [i] 0..4
     */
    fun code(i:Int):Byte{
        if (codeString == "") {
            return -1
        }
        return char2ByteMap[codeString[i].toInt()]
    }
}


fun main() {
    println(ChineseCharInfos['朝'])
}