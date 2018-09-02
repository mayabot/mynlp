package com.mayabot.nlp.segment.analyzer;

public abstract class BaseWordTermGeneratorWraper implements WordTermGenerator {

    protected WordTermGenerator base;

    public BaseWordTermGeneratorWraper(WordTermGenerator base) {
        this.base = base;
    }


}
