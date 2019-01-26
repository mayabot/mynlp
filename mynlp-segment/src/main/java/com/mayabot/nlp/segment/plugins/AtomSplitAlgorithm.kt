package com.mayabot.nlp.segment.plugins

import com.google.common.collect.Lists
import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap
import com.mayabot.nlp.hppc.QuickCharset
import com.mayabot.nlp.segment.Nature
import com.mayabot.nlp.segment.SegmentComponentOrder
import com.mayabot.nlp.segment.WordSplitAlgorithm
import com.mayabot.nlp.segment.common.BaseSegmentComponent
import com.mayabot.nlp.segment.common.String2
import com.mayabot.nlp.segment.wordnet.Wordnet
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * 高性能多模式识别。
 * 利用DAT的模式识别，从文本中一次性识别出多种模式。性能非常划算，避免了使用多个正则表达式。
 * 数字
 * 英文单词
 * 中文数字
 * 时间短语
 * Email
 *
 * @author jimichan
 */
class AtomSplitAlgorithm : BaseSegmentComponent(), WordSplitAlgorithm {

    //CharScatterSet 5000万次查询耗时40ms
    val chineseNumSet = QuickCharset(
            '零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '两',
            '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖', '拾',
            '十', '百', '千', '万', '亿'
    )


//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val wordnet = Wordnet("这个是你jimi@mayabot.com邮箱地址么2017-10-12".toCharArray())
////            val wordnet = Wordnet("你的ipad3么 ,最近三天花了多少钱 a-ff  -102 @163.com,一万八千八百八十八,FM98.1，jimi@mayabot.com,周一下午九点钟,一九九八年三月，2018年2月2日,2013年,周一下午三点半有个重量为11225.6公斤,123234".toCharArray())
//            val x = AtomSplitAlgorithm()
//            x.fill(wordnet)
//
//            println(wordnet.toMoreString())
//        }
//    }

    val dat: DoubleArrayTrieStringIntMap

    val emailPattern = Pattern.compile("[NA]+@[NA]+NA+")

    //fm101.1 iphone7 fm-981 a-b-c
    val xPattern = Pattern.compile("A+[N\\-][N\\-A]*")

    init {

        this.order = SegmentComponentOrder.DEFAULT

        val treeMap = TreeMap<String, Int>()

        fun TreeMap<String, Int>.addTemplate(template: String, type: Int) {
            parseTemplate(template).forEach { this[it] = type }
        }

        //时间
        treeMap["周Z"] = 1
        treeMap["星期Z"] = 1

        //年月日
        treeMap.addTemplate("{N[2,4]}年{N[1,2]}月{N[1,2]}{(日|号)}", 1)
        treeMap.addTemplate("{N[2,4]}-{N[2]}-{N[2]}", 1)

        //年
        treeMap.addTemplate("{N|Z[2,4]}{(年|年度)}", 1)

        //年月
        treeMap.addTemplate("{N|Z[2,4]}年{N[1,2]}月", 1)

        //月
        treeMap.addTemplate("{N|Z[1,2]}{(月|月份)}", 1)

        //月日
        treeMap.addTemplate("{N|Z[1,2]}月{N|Z[1-3]}{(日|号)}", 1)

        //日
        treeMap.addTemplate("{N[1,2]}{(日|号)}", 1)
        treeMap.addTemplate("{Z[1-3]}{(日|号)}", 1)

        //跨度时间 3天 三天
        treeMap.addTemplate("{N|Z[1-5]}天", 1)

        //x个月
        treeMap.addTemplate("{N|Z[1-5]}个月", 1)

        //5周
        treeMap.addTemplate("{N|Z[1-5]}周", 1)

        treeMap.addTemplate("{Z|N[1-2]}{(点|点钟|点半|点N刻)}", 1)

        treeMap.addTemplate("{N[1-2]}点{N[1-2]}分", 1)
        treeMap.addTemplate("{Z[1-3]}点{N[1-3]}分", 1)

        treeMap.addTemplate("{Z|N[1-3]}{(小时|个小时)}", 1)
        treeMap.addTemplate("{Z|N[1-3]}{(分|分钟)}", 1)
        treeMap.addTemplate("{Z|N[1-3]}{(秒|秒钟)}", 1)


        //数字 正负 整数 浮点数
        treeMap.addTemplate("{N[1-50]}", 2)
        treeMap.addTemplate("-{N[1-50]}", 2)

        treeMap.addTemplate("{Z[1-30]}", 2)
        treeMap.addTemplate("负{Z[1-30]}", 2)

        treeMap.addTemplate("{Z[1-20]}点{N[1,20]}", 2)
        treeMap.addTemplate("负{Z[1-20]}点{N[1,20]}", 2)

        //MQ 数量 一个 一双
        treeMap.addTemplate("{Z|N[1-10]}{(元|串|事|册|丘|下|丈|丝|举|具|包|厘|刀|分|列|则|剂|副|些|匝|队|部|出|个)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(介|令|份|伙|件|任|倍|儋|亩|记|双|发|叠|节|茎|通|造|遍|道)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(遭|对|尊|头|套|弓|引|张|弯|开|庄|床|座|庹|帖|帧|席|常|幅|幢|口|句|号|台|只|吊|合|名)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(吨|和|味|响|骑|门|间|阕|宗|客|家|彪|层|尾|届|声|扎|打|扣|把|抛|批|抔|抱|拨|担|拉|抬)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(拃|挂|挑|挺|捆|掬|排|捧|掐|提|握|摊|摞|撇|撮|汪|泓|泡|注|浔|派|湾|溜|滩|滴|级|纸|线)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(组|绞|统|绺|综|缕|缗|场|块|坛|垛|堵|堆|堂|塔|墩|回|团|围|圈|孔|贴|点|煎|熟|车|轮|转)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(载|辆|料|卷|截|户|房|所|扇|炉|炷|觉|斤|笔|本|朵|杆|束|条|杯|枚|枝|柄|栋|架|根|桄|梃)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(样|株|桩|梭|桶|棵|榀|槽|犋|爿|片|版|歇|手|拳|段|沓|班|文|曲|替|股|肩|脬|腔|支|步|武)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(瓣|秒|秩|钟|钱|铢|锊|铺|锤|锭|锱|章|盆|盏|盘|眉|眼|石|码|砣|碗|磴|票|罗|畈|番|窝|联)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(缶|耦|粒|索|累|緉|般|艘|竿|筥|筒|筹|管|篇|箱|簇|角|重|身|躯|酲|起|趟|面|首|项|领|顶|颗|顷|袭|群|袋)}", 3)

        treeMap.addTemplate("{Z|N[1-10]}{(公里|米|千米|厘米|毫米|微米|纳米|飞米|km|dm|cm|mm|μm|nm|fm)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(kg|dg|cg|mg|公斤|斤|克|毫克)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(dl|cl|ml|毫升|升)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(mhz|khz|赫兹)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(mpa|kpa|hpa|帕)}", 3)
        treeMap.addTemplate("{Z|N[1-10]}{(tb|gb|mb|kb|字节|兆)}", 3)
        treeMap.addTemplate("{N[1-10]}{A[1-3]}", 3)


        //单词
        treeMap.addTemplate("{A[2-100]}", 4)

        //连接符号
        treeMap.addTemplate("-", 5)

        dat = DoubleArrayTrieStringIntMap(treeMap)
    }

    override fun fill(wordnet: Wordnet) {
        val chars = wordnet.charArray

        //先扫描，判断是否包含数字、英文字母等必要元素
        var foundNum = false
        var foundAlpha = false
        var foundAt = false

        for (i in 0 until chars.size) {

            var c = chars[i]

            if (c < '{') {
                if (c >= '0' && c <= '9') {
                    foundNum = true
                } else if ((c >= 'a' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    foundAlpha = true
                }
            } else {
                if (chineseNumSet.contains(c)) {
                    foundNum = true
                }
            }
            if (foundAlpha || foundNum) {
                break
            }
        }

        var foundBigX = false
        if (foundNum || foundAlpha) {
            val newChars = Arrays.copyOf(chars, chars.size)
            for (i in 0 until chars.size) {
                var c = chars[i]
                if (c < '{') {
                    if (c >= '0' && c <= '9') {
                        newChars[i] = 'N'
                        continue
                    } else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
                        newChars[i] = 'A'
                        // _也算字母
                        continue
                    } else if (c == '.') {
                        // 把点也归一化为数字N的一种形式
                        newChars[i] = 'N'
                    } else if (c == '@') {
                        foundAt = true
                    }
                } else {
                    if (chineseNumSet.contains(c)) {
                        newChars[i] = 'Z'
                        continue
                    }
                }
            }

//            println(String(newChars))

            val match = dat.matchLong(newChars, 0)

            var bigXEnd = -1

            while (match.next()) {
                val type = match.value
                val offset = match.begin
                val length = match.length

                when (type) {
                    1 -> {
                        wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.t)
                    }
                    2 -> {
                        if (length == 1 && chars[offset] == '.') {

                        } else {
                            if (offset == bigXEnd) {
                                foundBigX = true
                            }
                            wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.m)
                        }
                    }
                    3 -> wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.mq)
                    4 -> {
                        wordnet.put(offset, length).setAbsWordNatureAndFreq(Nature.x)
                        bigXEnd = offset + length
                    }
                    5 -> {
                        //连接符号
                        if (offset == bigXEnd) {
                            foundBigX = true
                        }
                    }
                }


//                println("Found " + String(chars, match.begin, match.length))
            }


            if (foundAt) {
                var matcher = emailPattern.matcher(String2(newChars))
                while (matcher.find()) {
                    wordnet.put(matcher.start(), matcher.end() - matcher.start()).setAbsWordNatureAndFreq(Nature.x)
//                    println(matcher.group())
                }
            }

            if (foundBigX) {
                var matcher = xPattern.matcher(String2(newChars))
                while (matcher.find()) {
                    wordnet.put(matcher.start(), matcher.end() - matcher.start()).setAbsWordNatureAndFreq(Nature.x)
//                    println(matcher.group())
                }
            }
        }

    }


    /**
     * {Z[1,2]}
     * {Z[1-2]}
     * {(日|月)}
     */
    private fun parseTemplate(template: String): List<String> {
        val pattern = Regex("(\\{(.+?)\\})|(.+?)")

        val list = ArrayList<List<String>>()

        pattern.findAll(template).forEach { mr ->
            var part = mr.value
            if (part.startsWith("{") && part.endsWith("}")) {
                part = part.substring(1, part.length - 1)
            }

            if (part.startsWith("(") && part.endsWith(")")) {
                list.add(part.substring(1, part.length - 1).split("|").toList())
            } else if (part.contains("[") || part.contains("|")) {
                val st = ArrayList<String>()
                val e = part.substring(0, part.indexOf("[")).split("|")

                val range = part.substring(part.indexOf("[") + 1, part.lastIndexOf("]"))
                if (range.contains(",")) {
                    range.split(",").map { it.toInt() }.forEach { n ->
                        e.forEach { st.add(it.repeat(n)) }
                    }
                } else if (range.contains("-")) {
                    val start = range.split("-")[0].toInt()
                    val end = range.split("-")[1].toInt()
                    for (n in start..end) {
                        e.forEach { st.add(it.repeat(n)) }
                    }
                } else {
                    e.forEach { st.add(it.repeat(range.toInt())) }
                }
                list.add(st)
            } else {
                list.add(listOf(part))
            }
        }

        return Lists.cartesianProduct(list).map {
            it.joinToString(separator = "")
        }
    }

}
