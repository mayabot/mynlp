package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.MyTerm;
import com.mayabot.nlp.segment.MyAnalyzer;

import java.io.Reader;
import java.util.Iterator;

public abstract class WrapMyAnalyzer implements MyAnalyzer {

    protected MyAnalyzer myAnalyzer;

    public WrapMyAnalyzer(MyAnalyzer myAnalyzer) {
        this.myAnalyzer = myAnalyzer;
    }

    @Override
    public MyAnalyzer reset(Reader reader) {
        return myAnalyzer.reset(reader);
    }

    @Override
    public Iterator<MyTerm> iterator() {
        return myAnalyzer.iterator();
    }
}
