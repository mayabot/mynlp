package com.mayabot.nlp.segment.segment;

import com.mayabot.nlp.segment.MynlpSegment;
import com.mayabot.nlp.segment.MynlpTerm;

public abstract class WrapMyAnalyzerFilter extends WrapMyAnalyzer {

    public WrapMyAnalyzerFilter(MynlpSegment myAnalyzer) {
        super(myAnalyzer);
    }

    @Override
    public MynlpTerm next() {
        MynlpTerm next = myAnalyzer.next();
        while (next != null) {
            if (accept(next)) {
                return next;
            } else {
                next = myAnalyzer.next();
            }
        }
        return null;
    }

    abstract boolean accept(MynlpTerm term);
}
