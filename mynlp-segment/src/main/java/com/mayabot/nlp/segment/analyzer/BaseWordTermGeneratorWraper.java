package com.mayabot.nlp.segment.analyzer;

/**
 * @author jimichan
 */
public abstract class BaseWordTermGeneratorWraper implements WordTermGenerator {

    protected WordTermGenerator base;

    public BaseWordTermGeneratorWraper(WordTermGenerator base) {
        this.base = base;
    }


}
