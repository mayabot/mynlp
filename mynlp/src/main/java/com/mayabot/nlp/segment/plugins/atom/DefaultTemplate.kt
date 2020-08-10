package com.mayabot.nlp.segment.plugins.atom

import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap
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

    //月日
    treeMap.addTemplate("{N|Z[1,2]}月{N|Z[1-3]}{(日|号)}", TIME)

    //日
    treeMap.addTemplate("{N[1,2]}{(日|号)}", TIME)
    treeMap.addTemplate("{Z[1-3]}{(日|号)}", TIME)

    //跨度时间 3天 三天
    treeMap.addTemplate("{N|Z[1-5]}天", TIME)

    //x个月
    treeMap.addTemplate("{N|Z[1-5]}个月", TIME)

    //5周
    treeMap.addTemplate("{N|Z[1-5]}周", TIME)

    treeMap.addTemplate("{Z|N[1-2]}{(点|点钟|点半|点N刻)}", TIME)

    treeMap.addTemplate("{N[1-2]}点{N[1-2]}分", TIME)
    treeMap.addTemplate("{Z[1-3]}点{N[1-3]}分", TIME)

    treeMap.addTemplate("{Z|N[1-3]}{(小时|个小时)}", TIME)
    treeMap.addTemplate("{Z|N[1-3]}{(分|分钟)}", TIME)
    treeMap.addTemplate("{Z|N[1-3]}{(秒|秒钟)}", TIME)


    //数字 正负 整数 浮点数
    treeMap.addTemplate("{N[1-50]}", NUMBER)
    treeMap.addTemplate("-{N[1-50]}", NUMBER)

    treeMap.addTemplate("{Z[1-30]}", NUMBER)
    treeMap.addTemplate("负{Z[1-30]}", NUMBER)

    treeMap.addTemplate("{Z[1-20]}点{N[1,20]}", NUMBER)
    treeMap.addTemplate("负{Z[1-20]}点{N[1,20]}", NUMBER)

    //MQ 数量 一个 一双
    treeMap.addTemplate("{Z|N[1-10]}{(元|串|事|册|丘|下|丈|丝|举|具|包|厘|刀|分|列|则|剂|副|些|匝|队|部|出|个)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(介|令|份|伙|件|任|倍|儋|亩|记|双|发|叠|节|茎|通|造|遍|道)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(遭|对|尊|头|套|弓|引|张|弯|开|庄|床|座|庹|帖|帧|席|常|幅|幢|口|句|号|台|只|吊|合|名)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(吨|和|味|响|骑|门|间|阕|宗|客|家|彪|层|尾|届|声|扎|打|扣|把|抛|批|抔|抱|拨|担|拉|抬)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(拃|挂|挑|挺|捆|掬|排|捧|掐|提|握|摊|摞|撇|撮|汪|泓|泡|注|浔|派|湾|溜|滩|滴|级|纸|线)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(组|绞|统|绺|综|缕|缗|场|块|坛|垛|堵|堆|堂|塔|墩|回|团|围|圈|孔|贴|点|煎|熟|车|轮|转)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(载|辆|料|卷|截|户|房|所|扇|炉|炷|觉|斤|笔|本|朵|杆|束|条|杯|枚|枝|柄|栋|架|根|桄|梃)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(样|株|桩|梭|桶|棵|榀|槽|犋|爿|片|版|歇|手|拳|段|沓|班|文|曲|替|股|肩|脬|腔|支|步|武)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(瓣|秒|秩|钟|钱|铢|锊|铺|锤|锭|锱|章|盆|盏|盘|眉|眼|石|码|砣|碗|磴|票|罗|畈|番|窝|联)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(缶|耦|粒|索|累|緉|般|艘|竿|筥|筒|筹|管|篇|箱|簇|角|重|身|躯|酲|起|趟|面|首|项|领|顶|颗|顷|袭|群|袋)}", MQ)

    treeMap.addTemplate("{Z|N[1-10]}{(公里|米|千米|厘米|毫米|微米|纳米|飞米|km|dm|cm|mm|μm|nm|fm)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(kg|dg|cg|mg|公斤|斤|克|毫克)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(dl|cl|ml|毫升|升)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(mhz|khz|赫兹)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(mpa|kpa|hpa|帕)}", MQ)
    treeMap.addTemplate("{Z|N[1-10]}{(tb|gb|mb|kb|字节|兆)}", MQ)
    treeMap.addTemplate("{N[1-10]}{A[1-3]}", MQ)

    //单词
    treeMap.addTemplate("{A[2-100]}", WORD)

    //连接符号
    treeMap.addTemplate("-", CONNECT)

    val fr = TreeMap(treeMap.map { it.key to it.value.ordinal }.toMap())

    return DoubleArrayTrieStringIntMap(fr)
}