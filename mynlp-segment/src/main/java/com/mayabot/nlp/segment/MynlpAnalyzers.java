package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.analyzer.BaseMynlpAnalyzer;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
import com.mayabot.nlp.segment.analyzer.WordTermGenerator;

/**
 * MynlpAnalyzers
 *
 * @author jimichan
 */
public class MynlpAnalyzers {

    /**
     * 准分词
     *
     * @param tokenizer
     * @return
     */
    public static MynlpAnalyzer standard(MynlpTokenizer tokenizer) {
        return new StandardMynlpAnalyzer(tokenizer);
    }

    public static MynlpAnalyzer standard() {
        return new StandardMynlpAnalyzer();
    }

    public static MynlpAnalyzer base(MynlpTokenizer tokenizer) {
        return new BaseMynlpAnalyzer(tokenizer) {
            @Override
            protected WordTermGenerator warp(WordTermGenerator base) {
                return base;
            }
        };
    }
}
