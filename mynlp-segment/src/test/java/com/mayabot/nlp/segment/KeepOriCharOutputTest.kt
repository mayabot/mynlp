package com.mayabot.nlp.segment

import org.junit.Assert
import org.junit.Test

class KeepOriCharOutputTest {

    @Test
    fun test(){
        val lerxer = Lexers.coreBuilder()
                .keepOriCharOutput()
                .build()
        Assert.assertEquals("看看 下面 这 中文 逗号 ， Keep 大小写",
                lerxer.scan("看看下面这中文逗号，Keep 大小写").toPlainString()
        )
    }

    @Test
    fun test2(){
        val lerxer = Lexers.coreBuilder()
                .build()
        Assert.assertEquals("看看 下面 这 中文 逗号 , keep 大小写",
                lerxer.scan("看看下面这中文逗号，Keep 大小写").toPlainString()
        )
    }
}