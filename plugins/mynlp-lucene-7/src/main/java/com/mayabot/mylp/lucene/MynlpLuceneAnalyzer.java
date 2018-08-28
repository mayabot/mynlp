package com.mayabot.mylp.lucene;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.mayabot.nlp.segment.MynlpAnalyzer;
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

    static {
        try {
            URL url = Resources.getResource("maya_data/dictionary/stopwords.txt");
            BufferedReader reader = Resources.asCharSource(url, Charsets.UTF_8).openBufferedStream();
            STOP_WORDS_SET = loadStopwordSet(reader);
        } catch (Exception e) {
        }
    }

    private CharArraySet stopWordsSet = STOP_WORDS_SET;

    private MynlpAnalyzer mynlpAnalyzer;


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
        //this(new DefaultMynlpAnalyzer(MynlpSegments.nlpTokenizer(MynlpLuceneTokenizerFactory.mynlp)));
        //FIXME xxxx
    }

    public MynlpLuceneAnalyzer(MynlpAnalyzer mynlpAnalyzer) {
        this.mynlpAnalyzer = mynlpAnalyzer;
    }


    @Override
    protected TokenStreamComponents createComponents(final String fieldName) {
        final MynlpLuceneTokenizer src = new MynlpLuceneTokenizer(mynlpAnalyzer);
        TokenStream tok = new StandardFilter(src);
        tok = new LowerCaseFilter(tok);
        if (stopWordsSet != null) {
            tok = new StopFilter(tok, stopWordsSet);
        }

        return new TokenStreamComponents(src, tok);
    }


}
