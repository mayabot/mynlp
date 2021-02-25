package com.mayabot.nlp.module.pinyin.model

import com.mayabot.nlp.module.pinyin.model.Shengmu.*

/**
 * 声母相似
 **/

object PinyinFuzzy {
    private val shengmuMap = HashMap<Shengmu, List<Shengmu>>(Shengmu.values().size * 2)
    private val yummuMap = HashMap<Yunmu, List<Yunmu>>(Yunmu.values().size * 2)

    init {
        values().forEach {
            shengmuMap[it] = listOf()
        }

        infix fun Shengmu.like(b: Shengmu) {
            shengmuMap[this] = listOf(b)
            shengmuMap[b] = listOf(this)
        }

        z like zh
        s like sh
        c like ch

        l like n
        f like h
        r like l

        k like g
    }

    init {
        Yunmu.values().forEach {
            yummuMap[it] = listOf()
        }

        infix fun Yunmu.like(b: Yunmu) {
            yummuMap[this] = listOf(b)
            yummuMap[b] = listOf(this)
        }

        Yunmu.an like Yunmu.ang
        Yunmu.en like Yunmu.eng
        Yunmu.`in` like Yunmu.ing
        Yunmu.ian like Yunmu.iang
        Yunmu.uan like Yunmu.uang
    }

    private val pinyinFuzzyArray: Array<List<SimplePinyin>>

    private val pinyinFuzzyArray2: Array<List<SimplePinyin>>

    init {
        val pinyinFuzzyArray = Array<ArrayList<SimplePinyin>>(SimplePinyin.values().size) { ArrayList() }
        val index = HashMap<Shengmu, HashMap<Yunmu, SimplePinyin>>()

        SimplePinyin.values().forEach {

            index.getOrPut(it.shengmu) { HashMap() }
                .put(it.yunmu, it)
        }

        SimplePinyin.values().forEach {
            val slist = it.shengmu.fuzzy() + it.shengmu
            val ylist = it.yunmu.fuzzy() + it.yunmu
            val list = ArrayList<SimplePinyin>()
            for (s in slist) {
                for (y in ylist) {
                    val py = index[s]!![y]
                    if (py != null) {
                        if (py != it) {
                            list += py
                        }
                    }
                }
            }
            pinyinFuzzyArray[it.ordinal] = list
        }

        //TODO 这里可以加载自定义模糊拼音 hui->fei

        this.pinyinFuzzyArray = pinyinFuzzyArray.toList().map { it.toList() }.toTypedArray()

        /**
         * 包含自己
         */
        this.pinyinFuzzyArray2 = pinyinFuzzyArray.toList().mapIndexed { index, arrayList ->
            (arrayList + SimplePinyin.values()[index]).toList()
        }.toTypedArray()
    }

    /**
     * 查询模糊韵母
     */
    @JvmStatic
    fun fuzzy(a: Shengmu): List<Shengmu> {
        return shengmuMap[a] ?: listOf()
    }

    @JvmStatic
    fun fuzzy(a: Yunmu): List<Yunmu> {
        return yummuMap[a] ?: listOf()
    }

    @JvmStatic
    fun fuzzy(py: SimplePinyin): List<SimplePinyin> {
        return pinyinFuzzyArray[py.ordinal]
    }

    @JvmStatic
    fun fuzzy2(py: SimplePinyin): List<SimplePinyin> {
        return pinyinFuzzyArray2[py.ordinal]
    }
}

fun Shengmu.fuzzy() = PinyinFuzzy.fuzzy(this)

fun Yunmu.fuzzy() = PinyinFuzzy.fuzzy(this)

fun SimplePinyin.fuzzy() = PinyinFuzzy.fuzzy(this)

fun SimplePinyin.fuzzy2() = PinyinFuzzy.fuzzy2(this)




