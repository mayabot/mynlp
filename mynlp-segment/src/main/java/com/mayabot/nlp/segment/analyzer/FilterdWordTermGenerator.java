package com.mayabot.nlp.segment.analyzer;

import com.mayabot.nlp.segment.WordTerm;

import java.util.function.Predicate;

public abstract class FilterdWordTermGenerator extends BaseWordTermGeneratorWraper implements Predicate<WordTerm> {

    public FilterdWordTermGenerator(WordTermGenerator base) {
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
