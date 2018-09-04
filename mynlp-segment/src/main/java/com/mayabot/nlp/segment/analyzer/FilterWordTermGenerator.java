package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.WordTerm;

import java.util.function.Predicate;

/**
 * @author jimichan
 */
public abstract class FilterWordTermGenerator extends BaseWordTermGeneratorWraper implements Predicate<WordTerm> {

    public FilterWordTermGenerator(WordTermGenerator base) {
        super(base);
    }

    @Override
    public WordTerm nextWord() {


        WordTerm next = base.nextWord();
        while (next != null) {
            if (test(next)) {
                return next;
            } else {
                next = base.nextWord();
            }
        }
        return null;

    }

}
