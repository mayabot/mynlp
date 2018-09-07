package com.mayabot.mylp.lucene;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizers;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;

import java.io.BufferedReader;
import java.net.URL;

public class MynlpLuceneAnalyzer extends StopwordAnalyzerBase {

    /**
     * An unmodifiable set containing some common English words that are not usually useful
     * for searching.
     */
    public static CharArraySet STOP_WORDS_SET;


    //TODO 底层已经处理了停用词逻辑，这里需要整合一下

    static {
        try {
            URL url = Resources.getResource("maya_data/dictionary/stopwords.txt");
            BufferedReader reader = Resources.asCharSource(url, Charsets.UTF_8).openBufferedStream();
            STOP_WORDS_SET = loadStopwordSet(reader);
        } catch (Exception e) {
        }
    }

    private CharArraySet stopWordsSet = STOP_WORDS_SET;

    private MynlpTokenizer tokenizer;


    /**
     * 设置自定义的停用词. 可以设置为NULL。表示不启用停用词过滤。
     * 默认启用提用词
     *
     * @param stopWordsSet
     */
    public void setStopWordsSet(CharArraySet stopWordsSet) {
        this.stopWordsSet = stopWordsSet;
    }

    public MynlpLuceneAnalyzer() {
        this(MynlpTokenizers.coreTokenizer());
    }

    public MynlpLuceneAnalyzer(MynlpTokenizer mynlpTokenizer) {
        this.tokenizer = mynlpTokenizer;
    }


    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {

        final MynlpLuceneTokenizer src = new MynlpLuceneTokenizer(tokenizer);

        TokenStream tok = new StandardFilter(src);
        tok = new LowerCaseFilter(tok);
        if (stopWordsSet != null) {
            tok = new StopFilter(tok, stopWordsSet);
        }

        return new TokenStreamComponents(src, tok);
    }


}
