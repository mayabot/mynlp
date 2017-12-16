package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.MyAnalyzer;
import com.mayabot.nlp.segment.MyTerm;

public abstract class WrapMyAnalyzerFilter extends WrapMyAnalyzer {

    public WrapMyAnalyzerFilter(MyAnalyzer myAnalyzer) {
        super(myAnalyzer);
    }

    @Override
    public MyTerm next() {
        MyTerm next = myAnalyzer.next();
        while (next != null) {
            if(accept(next)){
                return next;
            }else{
                next = myAnalyzer.next();
            }
        }
        return null;
    }

     abstract boolean accept(MyTerm term);
}
