package com.mayabot.nlp.module.pinyin

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.module.pinyin.model.*
import kotlin.math.abs

//fun main() {
////    var dsl = 0.0
////    SimplePinyin.values().forEach { p1->
////        SimplePinyin.values().forEach { p2->
////            if(p1!=SimplePinyin.none && p2 !=SimplePinyin.none ){
////                val x = PinyinDistance.distance(p1,p2)
////                if (x<=1){
////                    println("$p1 @ $p2 $x")
////                }
////            }
////        }
////    }
////    println(dsl)
//    println(PinyinDistance.distanceSimple(
//        listOf(SimplePinyin.lu, SimplePinyin.lu),
//        listOf(SimplePinyin.ru, SimplePinyin.ru)
//    ))
//}

object PinyinDistance {

    private val hardCodeMap: Map<SimplePinyin, SimplePinyin> = kotlin.run {
        val map = HashMap<SimplePinyin, SimplePinyin>()

        listOf("/mynlp/py_hard_code_map.txt", "/mynlp/py_hard_code_map_ext.txt").forEach { name ->
            PinyinDistance::class.java.getResourceAsStream(name)?.bufferedReader(Charsets.UTF_8)
                ?.let {
                    it.use {
                        val list = it.readLines().map { it.trim() }.filter { !it.startsWith("#") && it.isNotBlank() }
                            .reversed()
                        list.forEach { line ->
                            val (x1, x2) = line.split("->", "-").map { it.trim() }.filter { it.isNotBlank() }
                                .map { SimplePinyin.valueOf(it) }
                            if (line.contains("->")) {
                                map[x1] = x2
                            } else {
                                map[x1] = x2
                                map[x2] = x1
                            }
                        }
                    }
                }
        }

        map.toMap()
    }

    fun distance(sen1: String, sen2: String): Float {
        val t1 = Mynlp.instance().convertPinyin(sen1).pinyinList
        val t2 = Mynlp.instance().convertPinyin(sen2).pinyinList
        return distance(sen1, sen2, t1, t2)
    }

//    fun distance(a: Pinyin, b: Pinyin): Float {
//        return distance(listOf(a), listOf(b))
//    }
//
//    fun distance(a: SimplePinyin, b: SimplePinyin): Float {
//        return distanceSimple(listOf(a), listOf(b))
//    }

    fun distanceSimple(text1: String, text2: String): Float {
        var res = Double.MAX_VALUE
        for (group1 in text1.textAllPinyin()) {
            for (group2 in text2.textAllPinyin()) {
                val dis = distanceSimple(text1, text2, group1, group2)
                if (dis < res) {
                    res = dis.toDouble()
                }
            }
        }
        return res.toFloat()
    }

    /**
     *
     */
    fun distanceSimple(aText: String, bText: String, aPy: List<SimplePinyin>, bPy: List<SimplePinyin>): Float {
        check(aPy.size == bPy.size)
        check(aText.length == bText.length)

        val tot = aPy.size * 2
        var numDiff = 0.0
        var res = 0.0
        for (i in 0 until aPy.size) {
            val apy = aPy[i]
            val bpy = bPy[i]

            if (apy == SimplePinyin.none || bpy == SimplePinyin.none) {
                if (aText[i] != bText[i]) {
                    numDiff += 2
                }
            } else {
                res += editDistanceClose2dCode(apy, bpy)

                if (apy.shengmu != bpy.shengmu) {
                    numDiff += 1
                }

                if (apy.yunmu != bpy.yunmu) {
                    numDiff += 1
                }
            }
        }

        val diffRatio = numDiff / tot
        return (res * diffRatio).toFloat()
    }

    fun distance(aText: String, bText: String, a: List<Pinyin>, b: List<Pinyin>): Float {
        check(a.size == b.size)
        val tot = a.size * 2.1
        var numDiff = 0.0
        var res = 0.0
        for (i in 0 until a.size) {
            val apy = a[i]
            val bpy = b[i]

            if (apy == Pinyin.none5 || bpy == Pinyin.none5) {
                if (aText[i] != bText[i]) {
                    numDiff += 2.1
                }
            } else {
                res += editDistanceClose2dCode(apy, bpy)

                if (apy.shengmu != bpy.shengmu) {
                    numDiff += 1
                }

                if (apy.yunmu != bpy.yunmu) {
                    numDiff += 1
                }
                if (apy.tone != bpy.tone) {
                    numDiff += 0.1
                }
            }

        }

        val diffRatio = numDiff / tot
        return (res * diffRatio).toFloat()
    }

    /**
     * 求两个拼音的 dimsim 算法的编辑距离
     * def get_edit_distance_close_2d_code(a, b):
     * res = 0
     * try:
     * if (a is None) or (b is None):
     * print("Error:pinyin({},{})".format(a.toString(),b.toString()))
     * return res
     *
     * twoDcode_consonant_a = consonantMap_TwoDCode[a.consonant]
     * twoDcode_consonant_b = consonantMap_TwoDCode[b.consonant]
     *
     * cDis = abs(get_distance_2d_code(twoDcode_consonant_a, twoDcode_consonant_b))
     *
     * twoDcode_vowel_a = vowelMap_TwoDCode[a.vowel]
     * twoDcode_vowel_b = vowelMap_TwoDCode[b.vowel]
     *
     * vDis = abs(get_distance_2d_code(twoDcode_vowel_a, twoDcode_vowel_b))
     *
     * hcDis = get_sim_dis_from_hardcod_map(a,b)
     *
     * res = min((cDis+vDis),hcDis) + 1.0*abs(a.tone-b.tone)/10
     *
     * 两个拼音 声母的 2d 维度 欧氏距离
     * 不考虑 声调
     * except:
     * raise Exception("Error pinyin {}{}".format(a.toString(), b.toString()))
     * return res
     * @param a
     * @param ba1
     * @return
     */
    fun editDistanceClose2dCode(a: SimplePinyin, b: SimplePinyin): Float {

        if (a == SimplePinyin.none || b == SimplePinyin.none) {
            error("editDistanceClose2dCode not for none")
        }

        val cDis: Double = abs(get_distance_2d_code(a.shengmu, b.shengmu))
        val vDis: Double = abs(get_distance_2d_code(a.yunmu, b.yunmu))

        val hcDis: Double = get_sim_dis_from_hardcod_map(a, b)

//        val res = Math.min( (cDis+vDis) , hcDis) + 1.0 * abs(a.tone-b.tone)/10.0
        val res = Math.min((cDis + vDis), hcDis)

        return res.toFloat()
    }

    fun editDistanceClose2dCode(a: Pinyin, b: Pinyin): Float {

        if (a.simple == SimplePinyin.none || b.simple == SimplePinyin.none) {
            error("editDistanceClose2dCode not for none")
        }

        val cDis: Double = abs(get_distance_2d_code(a.shengmu, b.shengmu))
        val vDis: Double = abs(get_distance_2d_code(a.yunmu, b.yunmu))

        val hcDis: Double = get_sim_dis_from_hardcod_map(a.simple, b.simple)

        val res = Math.min((cDis + vDis), hcDis) + 1.0 * abs(a.tone - b.tone) / 10.0

        return res.toFloat()
    }

    private fun get_sim_dis_from_hardcod_map(a: SimplePinyin, b: SimplePinyin): Double {
        if (hardCodeMap[a] == b || hardCodeMap[b] == a) {
            return 2.0
        } else {
            return Double.MAX_VALUE
        }
    }


    /**
     * def get_distance_2d_code(X, Y):
     * x1, x2 = X
     * y1, y2 = Y
     *
     * x1d = abs(x1-y1)
     * x2d = abs(x2-y2)
     *
     * return math.sqrt( x1d**2 + x2d**2)
     */
    private fun get_distance_2d_code(x: Shengmu, y: Shengmu): Double {
        if (x == y) return 0.0
        val x1 = x.twoDCode1.toDouble()
        val x2 = x.twoDCode2.toDouble()
        val y1 = y.twoDCode1.toDouble()
        val y2 = y.twoDCode2.toDouble()
        return Math.sqrt(Math.pow(Math.abs(x1 - y1), 2.0) + Math.pow(Math.abs(x2 - y2), 2.0))
    }

    private fun get_distance_2d_code(x: Yunmu, y: Yunmu): Double {
        if (x == y) return 0.0
        val x1 = x.twoDCode1.toDouble()
        val x2 = x.twoDCode2.toDouble()
        val y1 = y.twoDCode1.toDouble()
        val y2 = y.twoDCode2.toDouble()
        return Math.sqrt(Math.pow(Math.abs(x1 - y1), 2.0) + Math.pow(Math.abs(x2 - y2), 2.0))
    }
}