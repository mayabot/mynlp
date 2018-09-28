package com.mayabot.nlp.perceptron;

public interface PerceptronDataParser<T,E extends Enum<E>> {

    /**
     * 训练一个序列。在一个语料库训练中，改方法需要被调用很多次。
     *
     * @param source 语料库
     */
    PerceptronTrainer parse(Iterable<SequenceLabel<T>> source);

}