package com.mayabot.nlp.segment

import org.junit.Test

/**
 * 收集分词异常报错
 */
class SegmentErrorCasesTest {

    @Test
    fun carwords() {
        val tokenizer = Lexers.core()
        val lines = arrayOf(
                "你好离合器片的生产日期是2013-05-034S回复人635110101001",
                "第一次维修更换中间轴前轴承和倒档惰轮总成第二次是20170年6",
                "六万一千公里",
                "此车20171年12月19号来我站报修前照灯进水",
                "我站一辆宝骏5602017年2月16日到我站反映六档挡不进档")


        for (s in lines) {
            println(s)
            println(tokenizer.scan(s))
        }
    }
}
