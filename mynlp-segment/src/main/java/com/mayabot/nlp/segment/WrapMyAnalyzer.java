package com.mayabot.nlp.segment;

import java.io.Reader;

public abstract class WrapMyAnalyzer implements MyAnalyzer {

    private MyAnalyzer myAnalyzer;

    public WrapMyAnalyzer(MyAnalyzer myAnalyzer) {
        this.myAnalyzer = myAnalyzer;
    }

    @Override
    public MyAnalyzer reset(Reader reader) {
        return myAnalyzer.reset(reader);
    }

}
