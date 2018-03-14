package com.mayabot.nlp.analyzes;

import com.mayabot.nlp.segment.MynlpSegments;
import com.mayabot.nlp.segment.MynlpTokenizer;
import org.junit.Test;

/**
 * 收集分词异常报错
 */
public class SegmentErrorCasesTest {

    MynlpTokenizer tokenizer = MynlpSegments.nlpTokenizer();

    @Test
    public void test1() {
        //第一次维修更换中间轴前轴承和倒档惰轮总成第二次是20170年6
        //六万一千公里
        //此车20171年12月19号来我站报修前照灯进水
        //我站一辆宝骏5602017年2月16日到我站反映六档挡不进档
        String line = "你好   离合器片的生产日期是2013-05-034S回复人635110101001";

        String[] lines = new String[]{
                "第一次维修更换中间轴前轴承和倒档惰轮总成第二次是20170年6",
                "六万一千公里",
                "此车20171年12月19号来我站报修前照灯进水",
                "我站一辆宝骏5602017年2月16日到我站反映六档挡不进档",
        };
//        tokenizer.token(line.toCharArray()).forEach(it->{
        //
//            System.out.println(String.format("%d : %s",it.offset,it.word));
//        });
        tokenizer.token(line.toCharArray()).forEach(
                System.out::println);
    }


}
