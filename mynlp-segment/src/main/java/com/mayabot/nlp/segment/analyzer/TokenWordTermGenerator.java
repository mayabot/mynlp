package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.utils.FastCharReader;
import com.mayabot.nlp.utils.ParagraphReader;
import com.mayabot.nlp.utils.ParagraphReaderSmart;
import com.mayabot.nlp.utils.ParagraphReaderString;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * @author jimichan
 */
public class TokenWordTermGenerator implements WordTermGenerator {

    private MynlpTokenizer tokenizer;

    private ParagraphReader paragraphReader;

    /**
     * 段落的偏移位置
     */
    private int baseOffset = 0;

    /**
     * 内部变量
     */
    private int lastTextLength = -1;

    private LinkedList<WordTerm> buffer = new LinkedList();

    /**
     * 构造函数
     *
     * @param reader    需要分词的数据源
     * @param tokenizer 具体的分词器
     */
    public TokenWordTermGenerator(Reader reader, MynlpTokenizer tokenizer) {
        this.setReader(reader);
        this.tokenizer = tokenizer;
    }

    public TokenWordTermGenerator(String text, MynlpTokenizer tokenizer) {
        this.setString(text);
        this.tokenizer = tokenizer;
    }

    long time = 0;

    /**
     * 返回下一个词项，如果到达最后的结果，那么返回null
     *
     * @return
     */
    @Override
    public WordTerm nextWord() {

        if (buffer.isEmpty()) {

            String paragraph;
            try {
                paragraph = paragraphReader.next();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (paragraph == null || paragraph.isEmpty()) {
                return null;
            } else {
                if (lastTextLength == -1) {
                    this.baseOffset = 0;
                    this.lastTextLength = 0;
                } else {
                    this.baseOffset = lastTextLength;
                }

                lastTextLength += paragraph.length();

                char[] text = paragraph.toCharArray();

                tokenizer.token(text, term -> this.buffer.add(term));

            }
        }

        WordTerm term = buffer.pop();

        if (term == null) {
            return null;
        }
        //补充偏移量
        if (baseOffset != 0) {
            term.setOffset(term.getOffset() + baseOffset);
        }

        return term;
    }

    private void setReader(Reader reader) {
        //智能分段器
        this.paragraphReader = new ParagraphReaderSmart(
                new FastCharReader(reader, 128),
                1024);
        baseOffset = 0;
        lastTextLength = -1;
    }

    private void setString(String text) {
        this.paragraphReader = new ParagraphReaderString(text);
        baseOffset = 0;
        lastTextLength = -1;
    }

}
