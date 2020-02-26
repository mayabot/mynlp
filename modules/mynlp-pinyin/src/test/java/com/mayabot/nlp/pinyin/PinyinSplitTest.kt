package com.mayabot.nlp.pinyin

import com.mayabot.nlp.pinyin.split.PinyinSplits
import org.junit.Assert
import org.junit.Test

class PinyinSplitTest {


    @Test
    fun test() {
        Assert.assertEquals(
                PinyinSplits.split("lianhedianyinguanmoquan").joinToString(separator = ", "),
                "lian, he, dian, yin, guan, mo, quan"
        )
    }

    @Test
    fun test2(){
        println(PinyinSplits.split("lihailewodeguo"))
    }
}