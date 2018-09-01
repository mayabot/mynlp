package com.mayabot.nlp.segment.analyzer;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.CharNormalize;
import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpAnalyzerFactory;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.analyzer.normalize.Full2halfCharNormalize;
import com.mayabot.nlp.segment.analyzer.normalize.LowerCaseCharNormalize;

import java.io.Reader;
import java.util.List;
import java.util.Set;

/**
 * 一个标准的AnalyzerFactory.
 * 默认行为包括小写字母、全角转半角、停用词
 *
 * @author jimichan
 */
public class StandardAnalyzerFactory implements MynlpAnalyzerFactory {


    private MynlpTokenizer mynlpTokenizer;

    private Set<String> stopword = null;

    public StandardAnalyzerFactory(MynlpTokenizer mynlpTokenizer) {
        this.mynlpTokenizer = mynlpTokenizer;
    }

    public StandardAnalyzerFactory(MynlpTokenizer mynlpTokenizer, Set<String> stopword) {
        this.mynlpTokenizer = mynlpTokenizer;
        this.stopword = stopword;
    }

    static List<CharNormalize> charNormalizes = Lists.newArrayList(LowerCaseCharNormalize.instance, Full2halfCharNormalize.instace);

    @Override
    public MynlpAnalyzer create(Reader reader) {

        BaseMynlpAnalyzer base = new BaseMynlpAnalyzer(reader, mynlpTokenizer);

        base.setCharNormalize(charNormalizes);

        MynlpAnalyzer analyzer = new PunctuationFilter(base);

        analyzer = new StopwordFilter(analyzer, stopword);

        return analyzer;
    }

}
