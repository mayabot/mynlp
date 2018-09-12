package com.mayabot.mylp.lucene;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author jimichan
 */
public class MynlpLuceneTokenizer extends Tokenizer {

    /**
     * 当前词
     */
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    /**
     * 偏移量
     */
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    /**
     * 距离
     */
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);


    private Iterator<WordTerm> iterator;

    private MynlpAnalyzer analyzer;

    public MynlpLuceneTokenizer(MynlpTokenizer tokenizer) {
        analyzer = new StandardMynlpAnalyzer(tokenizer);
    }

    public MynlpLuceneTokenizer(MynlpAnalyzer analyzer) {
        analyzer = analyzer;
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();

        if (iterator.hasNext()) {
            WordTerm next = iterator.next();

            positionAttr.setPositionIncrement(1);
            termAtt.setEmpty().append(next.word);
            offsetAtt.setOffset(next.getOffset(), next.getOffset() + next.length());

            return true;
        } else {
            return false;
        }
    }


    /**
     * 必须重载的方法，否则在批量索引文件时将会导致文件索引失败
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        Iterable<WordTerm> iterable = analyzer.parse(this.input);
        iterator = iterable.iterator();
    }

}
