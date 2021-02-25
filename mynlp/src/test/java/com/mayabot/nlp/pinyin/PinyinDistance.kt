package com.mayabot.nlp.pinyin

import com.mayabot.nlp.module.pinyin.PinyinDistance
import org.junit.Test

class PinyinDistance {

    @Test
    fun test() {
        PinyinDistance.distance("灰机", "飞机")
        PinyinDistance.distance("粉丝", "大侠")
        PinyinDistance.distance("粉丝中", "大侠梦")
    }
}