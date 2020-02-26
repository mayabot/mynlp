package com.mayabot.nlp.segment.plugins.atom

import com.mayabot.nlp.collection.dat.DoubleArrayTrieStringIntMap
import com.mayabot.nlp.segment.plugins.atom.TemplateType.*
import java.util.*

fun defaultTemplates(): DoubleArrayTrieStringIntMap {
    val treeMap = TreeMap<String, TemplateType>()


    //时间
    treeMap["周Z"] = TIME
    treeMap["星期Z"] = TIME

    //年月日
    treeMap.addTemplate("{N[2,4]}年{N[1,2]}月{N[1,2]}{(日|号)}", TIME)
    treeMap.addTemplate("{N[2,4]}-{N[2]}-{N[2]}", TIME)

    //年
    treeMap.addTemplate("{N|Z[2,4]}{(年|年度)}", TIME)

    //年月
    treeMap.addTemplate("{N|Z[2,4]}年{N[1,2]}月", TIME)

    //月
    treeMap.addTemplate("{N|Z[1,2]}{(月|月份)}", TIME)

    //日
    treeMap.addTemplate("{N[1,2]}{(日|号)}", TIME)
    treeMap.addTemplate("{Z[1-3]}{(日|号)}", TIME)

    //数字 正负 整数 浮点数
    treeMap.addTemplate("{N[1-50]}", NUMBER)
    treeMap.addTemplate("{N[1-50]}", NUMBER)

    treeMap.addTemplate("{第}{Z|N[1-10]}", NUMBER)

    treeMap.addTemplate("{Z[1-30]}", NUMBER)

    treeMap.addTemplate("{Z|N[1-10]}{(元|串|事|册|丘|下|丈|丝|举|具|包|厘|刀|分|列|则|剂|副|些|匝|队|部|出|个)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(介|令|份|伙|件|任|倍|儋|亩|记|双|发|叠|节|茎|通|造|遍|道)}", MQ)

    treeMap.addTemplate("{Z|N[1-10]}{(公里|米|千米|厘米|毫米|微米|纳米|飞米|km|dm|cm|mm|μm|nm|fm)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(kg|dg|cg|mg|公斤|斤|克)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(tb|gb|mb|kb|字节|兆)}", MQ)
    treeMap.addTemplate("{N[1-10]}{A[1-3]}", MQ)

    //单词
    treeMap.addTemplate("{A[2-100]}", WORD)

    //连接符号
    treeMap.addTemplate("-", CONNECT)

    val fr = TreeMap(treeMap.map { it.key to it.value.ordinal }.toMap())

    return DoubleArrayTrieStringIntMap(fr)
}