package com.mayabot.nlp.segment.segment;

import com.mayabot.nlp.segment.MynlpSegment;
import com.mayabot.nlp.segment.MynlpTerm;
import com.mayabot.nlp.utils.Characters;

/**
 * 过滤标点符号
 */
public class PunctuationFilter extends WrapMyAnalyzerFilter {

    @Override
    boolean accept(MynlpTerm term) {
        return !Characters.isPunctuation(term.word.charAt(0));
    }

    public PunctuationFilter(MynlpSegment myAnalyzer) {
        super(myAnalyzer);
    }
}
