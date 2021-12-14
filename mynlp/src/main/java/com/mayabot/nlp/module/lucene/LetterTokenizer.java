package com.mayabot.nlp.module.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.nio.CharBuffer;

/**
 * 单子分词
 *
 * @author jimichan
 */
final public class LetterTokenizer extends Tokenizer {

    /**
     * 当前词
     */
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    /**
     * 偏移量
     */
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    /**
     * Position Increment
     */
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);

    private final CharBuffer buffer = CharBuffer.allocate(64);
    private boolean firstRead = true;
    private int offsetChar = -1;


    /**
     * 返回下一个Token
     *
     * @return 是否有Token
     */
    @Override
    public boolean incrementToken() throws IOException {

        if (firstRead) {
            firstRead = false;
            int len = this.input.read(buffer);
            if (len <= 0) {
                return false;
            }
            buffer.flip();
        }

        clearAttributes();

        if (!buffer.hasRemaining()) {
            buffer.clear();
            int len = this.input.read(buffer);
            if (len <= 0) {
                return false;
            }
            buffer.flip();
        }

        char ch = buffer.get();
        offsetChar++;


        positionAttr.setPositionIncrement(1);
        termAtt.setEmpty().append(ch);
        offsetAtt.setOffset(offsetChar, offsetChar + 1);
        return true;
    }

    @Override
    public void end() throws IOException {
        super.end();
        this.offsetAtt.setOffset(offsetChar, offsetChar);
    }

    /**
     * This method is called by a consumer before it begins consumption using
     * {@link #incrementToken()}.
     * <p>
     * Resets this stream to a clean state. Stateful implementations must implement
     * this method so that they can be reused, just as if they had been created fresh.
     * <p>
     * If you override this method, always call {@code super.reset()}, otherwise
     * some internal state will not be correctly reset (e.g., {@link Tokenizer} will
     * throw {@link IllegalStateException} on further usage).
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        this.buffer.clear();
        firstRead = true;
        offsetChar = -1;
    }

}