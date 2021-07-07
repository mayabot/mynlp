package com.mayabot.nlp.pinyin

import com.mayabot.nlp.Mynlp
import com.mayabot.nlp.Mynlp.Companion.instance
import org.junit.Assert
import org.junit.Test

class PinyinTest {

    @Test
    fun test() {
        Assert.assertEquals("[zhao, zhao, mu, mu]", "朝朝暮暮".py())
    }

    @Test
    fun test2() {
        println(
            instance().convertPinyin("转战")
                .fuzzy(true).asList()
        )
    }

    private fun String.py() = Mynlp.instance().convertPinyin(this).asList().toString()
}