package com.mayabot.mylp.lucene;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpTerm;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.LinkedList;

public class MynlpLuceneTokenizer extends Tokenizer {

    // 当前词
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    // 偏移量
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    // 距离
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);

    private MynlpAnalyzer myAnalyzer;

    private LinkedList<MynlpTerm> buffer;

    public MynlpLuceneTokenizer(MynlpAnalyzer tokenizer) {
        myAnalyzer = tokenizer;
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();

        if (buffer != null && !buffer.isEmpty()) {
            MynlpTerm subword = buffer.pop();
            positionAttr.setPositionIncrement(1);
            termAtt.setEmpty().append(subword.word);
            offsetAtt.setOffset(subword.getOffset(), subword.getOffset() + subword.length());
            return true;
        }

        MynlpTerm term = myAnalyzer.next();

        if (term == null) {
            return false;
        }

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
        myAnalyzer.reset(this.input);
    }


}
