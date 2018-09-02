package com.mayabot.nlp.segment.analyzer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.WordTerm;
import com.mayabot.nlp.utils.ParagraphReader;
import com.mayabot.nlp.utils.ParagraphReaderSmart;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

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

    private MynlpTermBuffer buffer = new MynlpTermBuffer();

    private Reader reader;

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

    /**
     * 返回下一个词项，如果到达最后的结果，那么返回null
     *
     * @return
     */
    @Override
    public WordTerm nextWord() {

        if (!buffer.hasRemain()) {

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

                //如果buffer太大了就
                if (buffer.list.size() > 5000) {
                    buffer.list = Lists.newArrayListWithExpectedSize(256);
                }
                tokenizer.token(text, this.buffer.list);

                buffer.reset();
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

    static private class MynlpTermBuffer {
        ArrayList<WordTerm> list = Lists.newArrayListWithExpectedSize(256);
        int po = 0;
        int cap = 0;

        public boolean hasRemain() {
            return po < cap;
        }

        public WordTerm pop() {
            if (po >= cap) {
                return null;
            }
            return list.get(po++);
        }

        public void reset() {
            cap = list.size();
            po = 0;
        }
    }

    private void setReader(Reader reader) {
        this.reader = reader;
        //智能分段器
        this.paragraphReader = new ParagraphReaderSmart(reader);
        baseOffset = 0;
        lastTextLength = -1;
    }

//    public void close() {
////        if (reader != null) {
////            try {
////                reader.close();
////            } catch (IOException e) {
////                throw new RuntimeException("", e);
////            }
////        }
//        tokenizer = null;
//        reader = null;
//        this.paragraphReader = null;
//    }
}
