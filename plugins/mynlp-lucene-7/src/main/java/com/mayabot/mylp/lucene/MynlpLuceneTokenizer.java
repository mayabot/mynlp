package com.mayabot.mylp.lucene;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class MynlpLuceneTokenizer extends Tokenizer {

    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    // 偏移量
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    // 距离
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);


    private LinkedList<WordTerm> buffer;
    private Iterator<WordTerm> iterator;

    private StandardMynlpAnalyzer analyzer;

    public MynlpLuceneTokenizer(MynlpTokenizer tokenizer) {
        analyzer = new StandardMynlpAnalyzer(tokenizer);
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();

        if (buffer != null && !buffer.isEmpty()) {
            WordTerm subword = buffer.pop();
            positionAttr.setPositionIncrement(1);
            termAtt.setEmpty().append(subword.word);
            offsetAtt.setOffset(subword.getOffset(), subword.getOffset() + subword.length());
            return true;
        }

        if (iterator.hasNext()) {
            return false;
        }

        WordTerm term = iterator.next();

        if (term.getSubword() != null) {
            buffer = new LinkedList<>(term.getSubword());
        } else {
            buffer = null;
        }

        positionAttr.setPositionIncrement(1);

        termAtt.setEmpty().append(term.word);
        offsetAtt.setOffset(term.getOffset(), term.getOffset() + term.length());
        return true;
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
