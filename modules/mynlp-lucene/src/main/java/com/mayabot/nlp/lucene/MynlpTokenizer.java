package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.*;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Tokenizer的实现依赖Mynlp的LexerReader具体实现。
 * Tokenizer 接口兼容lucene5.0+，至少到7+还是兼容的。
 *
 * @author jimichan
 */
final public class MynlpTokenizer extends Tokenizer {

    private IterableMode mode = IterableMode.DEFAULT;

    public static Tokenizer fromLexer(LexerReader lexerReader) {
        return new MynlpTokenizer(lexerReader);
    }

    public static Tokenizer fromLexer(Lexer lexer) {
        return new MynlpTokenizer(lexer.reader());
    }

    /**
     * 当前词
     */
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    /**
     * 偏移量
     */
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    /**
     * Position Increment
     */
    private final PositionIncrementAttribute positionAttr = addAttribute(PositionIncrementAttribute.class);


    private final PositionLengthAttribute positionLenAttr = addAttribute(PositionLengthAttribute.class);

    private Iterator<WordTerm> iterator;

    private final LexerReader lexerReader;

    /**
     * Lucene Tokenizer的Mynlp插件实现
     *
     * @param lexerReader LexerReader
     */
    public MynlpTokenizer(LexerReader lexerReader) {
        this.lexerReader = lexerReader;
    }

    public MynlpTokenizer(LexerReader lexerReader,IterableMode mode) {
        this.lexerReader = lexerReader;
        this.mode = mode;
    }

    public IterableMode getMode() {
        return mode;
    }

    public MynlpTokenizer setMode(IterableMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * 返回下一个Token
     *
     * @return 是否有Token
     */
    @Override
    public boolean incrementToken() {
        clearAttributes();

        if (iterator.hasNext()) {
            WordTerm next = iterator.next();

            if (Nature.w == next.getNature()) {
                typeAtt.setType("Punctuation");
            }

            positionAttr.setPositionIncrement(next.getPosInc());
            termAtt.setEmpty().append(next.word);
            offsetAtt.setOffset(next.offset, next.offset + next.length());

            if (mode == IterableMode.GRAPH) {
                if (next.hasSubword()) {
                    positionLenAttr.setPositionLength(next.getSubword().size());
                }else{
                    positionLenAttr.setPositionLength(1);
                }
            }

            return true;
        } else {
            return false;
        }


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

        switch (mode) {
            case GRAPH:
                iterator = new GraphIterator(lexerReader.scan(this.input).iterator());
                break;
            case FLATTEN:
                iterator = new FlattenIterator(lexerReader.scan(this.input).iterator());
                break;
            case DEFAULT:
            default:
                iterator = lexerReader.scan(this.input).iterator();
        }

    }

}