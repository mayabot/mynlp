package com.mayabot.nlp.pinyin

import com.mayabot.nlp.Mynlp
import org.junit.Assert
import org.junit.Test

class PinyinTest {

    @Test
    fun test() {
        Assert.assertEquals("[zhao, zhao, mu, mu]", "朝朝暮暮".py())
    }

    private fun String.py() = Mynlp.instance().convertPinyin(this).asList().toString()
}