package com.mayabot.nlp.segment.atom

import com.mayabot.nlp.segment.plugins.atom.AtomSplitAlgorithm
import com.mayabot.nlp.segment.wordnet.Wordnet
import org.junit.Test


class SimpleTest {
    @Test
    fun unitTestingWorks() {
        val text = listOf("这个是你jimi@mayabot.com邮箱地址么2017-10-12",
                "你的ipad3么 ,最近三天花了多少钱 a-ff  -102 @163.com,一万八千八百八十八,FM98.1，jimi@mayabot.com,周一下午九点钟,一九九八年三月，2018年2月2日,2013年,周一下午三点半有个重量为11225.6公斤,123234"
        )
        val atom = AtomSplitAlgorithm()

        text.forEach { line ->
            val wordnet = Wordnet(line.toCharArray())

            atom.fill(wordnet)
            println(wordnet.toMoreString())
        }


    }
}