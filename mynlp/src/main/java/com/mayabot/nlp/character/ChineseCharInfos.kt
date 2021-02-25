package com.mayabot.nlp.character

import com.mayabot.nlp.module.pinyin.model.Pinyin
import com.mayabot.nlp.module.pinyin.model.SimplePinyin

/**
 * 新四角编码
 */
object ChineseCharInfos : Iterable<ChineseCharInfo> {

    private val array: Array<ChineseCharInfo?>

    /**
     * 存储了ChineseCharInfo的字符
     */
    @JvmStatic
    val chars: Set<Char>

    init {
        val codeArray = loadFromFile("/mynlp/char_four_code.txt")
        val structArray = loadFromFile("/mynlp/char_struct.txt")
        val writeNumArray = loadFromFile("/mynlp/char_write_num.txt")

        val pinyin = loadPinyin()

        this.array = Array(65535) { i ->
            val code = codeArray[i]
            val codeString = if (code == -1) {
                ""
            } else {
                code.toString().padStart(5, '0')
            }
            val st = structArray[i]
            val wn = writeNumArray[i]
            val py = pinyin[i]

            if (code == -1 && st == -1 && wn == -1 && py == null) {
                null
            } else {
                ChineseCharInfo(
                    i.toChar(),
                    codeString,
                    st,
                    wn,
                    py?.toList() ?: emptyList()
                )
            }
        }

        this.chars =
            array.filterNotNull().mapIndexed { index, _ -> index.toChar() }.toSet()

    }

    fun size(): Int {
        return chars.size
    }

    /**
     * 遍历所有的ChineseCharInfo
     */
    override fun iterator(): Iterator<ChineseCharInfo> {
        return object : AbstractIterator<ChineseCharInfo>() {
            val ite = chars.iterator()
            override fun computeNext() {
                if (ite.hasNext()) {
                    setNext(array[ite.next().toInt()]!!)
                } else {
                    done()
                }
            }
        }
    }

    @JvmStatic
    operator fun get(i: Int): ChineseCharInfo? = array[i]

    @JvmStatic
    operator fun get(i: Char): ChineseCharInfo? = array[i.toInt()]

    private fun loadFromFile(name: String): IntArray {
        val array = IntArray(65535) { -1 }
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

    private fun loadPinyin(): Array<Array<SimplePinyin>?> {
        val array = Array<Array<SimplePinyin>?>(65535) { null }
        ChineseCharInfos::class.java.getResourceAsStream("/mynlp/char_py.txt")
            .bufferedReader(Charsets.UTF_8)
            .useLines { lines ->
                lines.forEach { line ->
                    val (left, right) = line.split("=")
                    val pys = right.split(",").map { Pinyin.valueOf(it).simple }.toTypedArray()
                    val chuli = if (pys.size == 1) {
                        pys
                    } else {
                        pys.toSet().toTypedArray()
                    }
                    array[left[0].toInt()] = chuli
                }
            }
        return array
    }
}


data class ChineseCharInfo(

    val char: Char,

    /**
     * 5位四角码
     * 不存在的话，使用空字符
     */
    val code: String,

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
    val struct: Int,

    /**
     * 笔画
     * 不存在的话，等于-1
     */
    val writeNum: Int,

    val pinyin: List<SimplePinyin>

)


