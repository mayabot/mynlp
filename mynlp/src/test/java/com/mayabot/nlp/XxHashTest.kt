package com.mayabot.nlp

import net.openhft.hashing.LongHashFunction
import org.junit.Test

class XxHashTest {

    @Test
    fun test() {
        //7958582187431989116
        println(LongHashFunction.xx().hashChars("要闻汲取奋力前行力量李强龚正等参观我们众志成城上海防控新冠肺炎疫情主题展览"))
    }
}