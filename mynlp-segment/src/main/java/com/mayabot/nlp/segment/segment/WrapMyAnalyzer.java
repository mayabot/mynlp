package com.mayabot.nlp.segment.segment;

import com.mayabot.nlp.segment.MynlpSegment;
import com.mayabot.nlp.segment.MynlpTerm;

import java.io.Reader;
import java.util.Iterator;

public abstract class WrapMyAnalyzer implements MynlpSegment {

    protected MynlpSegment myAnalyzer;

    public WrapMyAnalyzer(MynlpSegment myAnalyzer) {
        this.myAnalyzer = myAnalyzer;
    }

    @Override
    public MynlpSegment reset(Reader reader) {
        return myAnalyzer.reset(reader);
    }

    @Override
    public Iterator<MynlpTerm> iterator() {
        return myAnalyzer.iterator();
    }
}
