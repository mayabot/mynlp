package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.Lexers;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Assert;
import org.junit.Test;

public class LuceneAnalyzerTest2 {

    @Test
    public void test() throws Exception {
        MynlpAnalyzer analyzer = new MynlpAnalyzer(
                Lexers.core().filterReader(true, true)
        );

        TokenStream tokenStream = analyzer.tokenStream("title", "俞正声主持召开全国政协第五十三次主席会议");
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
        System.out.println(sb.toString());
        analyzer.close();

    }
}
