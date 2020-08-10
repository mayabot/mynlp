package com.mayabot.nlp.module.lucene;

import com.mayabot.nlp.segment.FluentLexerBuilder;
import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.Lexers;
import com.mayabot.nlp.segment.WordTermIterableMode;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

public class LuceneAnalyzerTest2 {

    @Test
    public void test() throws Exception {
        FluentLexerBuilder builder = Lexers.coreBuilder();

        FluentLexerBuilder.CollectorBlock collector = builder.collector();
        collector.indexPickup().done();

        Lexer lexer = builder.build();

        System.out.println(lexer);

        MynlpAnalyzer analyzer = new MynlpAnalyzer(lexer.filterReader(true, true),
                WordTermIterableMode.ATOM
        );

        TokenStream tokenStream = analyzer.tokenStream("title", "北京大学");
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
