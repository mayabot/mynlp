package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.Analyzers;
import com.mayabot.nlp.segment.Tokenizers;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Assert;
import org.junit.Test;

public class UseAnalyzer {

    @Test
    public void test() throws Exception {
        MynlpLuceneAnalyzer analyzer = new MynlpLuceneAnalyzer(
                Analyzers.standard(Tokenizers.coreTokenizer())
        );

        TokenStream tokenStream = analyzer.tokenStream("title", "商品和服务，上海市副市长，Git有很多优势，其中之一就是远程操作非常简便。本文详细介绍5个Git命令，它们的概念和用法，理解了这些内容，你就会完全掌握Git远程操作。");
        tokenStream.reset();

        StringBuffer sb = new StringBuffer();

        while (tokenStream.incrementToken()) {
            sb.append(tokenStream.getAttribute(CharTermAttribute.class));
            sb.append("\t");
            sb.append(tokenStream.getAttribute(OffsetAttribute.class).startOffset());
            sb.append("\t");
            sb.append(tokenStream.getAttribute(PositionIncrementAttribute.class).getPositionIncrement());
            sb.append("\n");
        }

        analyzer.close();

        Assert.assertTrue(sb.toString().equals(
                "商品\t0\t1\n" +
                        "服务\t3\t2\n" +
                        "上海市\t6\t1\n" +
                        "副市长\t9\t1\n" +
                        "git\t13\t1\n" +
                        "很多\t17\t2\n" +
                        "优势\t19\t1\n" +
                        "远程\t28\t4\n" +
                        "操作\t30\t1\n" +
                        "非常\t32\t1\n" +
                        "简便\t34\t1\n" +
                        "本文\t37\t1\n" +
                        "详细\t39\t1\n" +
                        "介绍\t41\t1\n" +
                        "5个\t43\t1\n" +
                        "git\t45\t1\n" +
                        "命令\t48\t1\n" +
                        "概念\t54\t3\n" +
                        "用法\t57\t2\n" +
                        "理解\t60\t1\n" +
                        "内容\t65\t3\n" +
                        "会\t70\t3\n" +
                        "完全\t71\t1\n" +
                        "掌握\t73\t1\n" +
                        "git\t75\t1\n" +
                        "远程\t78\t1\n" +
                        "操作\t80\t1\n"));
    }
}
