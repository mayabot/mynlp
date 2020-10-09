package com.mayabot.nlp;

@FunctionalInterface
public interface MynlpIniter {
    /**
     * @param builder
     */
    void init(MynlpBuilder builder);
}
