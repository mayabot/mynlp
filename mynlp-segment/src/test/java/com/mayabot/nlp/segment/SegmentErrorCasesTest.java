package com.mayabot.nlp.segment;

import org.junit.Test;

/**
 * 收集分词异常报错
 */
public class SegmentErrorCasesTest {

    @Test
    public void carwords() {
        MynlpTokenizer tokenizer = MynlpTokenizers.coreTokenizer();
        String[] lines = new String[]{
                "你好|离合器|片|的|生产日期|是|2013-05-034S|回复人|635110101001",
                "第一|次|维修|更换|中间轴|前|轴承|和|倒|档|惰轮|总|成|第二|次|是|20170|年|6",
                "六万一千|公里",
                "此|车|20171|年|12月19号|来|我|站|报修|前照灯|进水",
                "我|站|一|辆|宝骏|5602017|年|2月|16|日|到|我|站|反映|六|档|挡|不|进|档",
        };

        for (String s : lines) {
            TokenizerTestHelp.test(tokenizer, s);
        }
    }


}
