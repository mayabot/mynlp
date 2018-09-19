package com.mayabot.nlp.perceptron;

/**
 * 感知机训练器。
 * 有状态的类，一次完整训练创建一个PerceptronTrainer对象。
 * 有两个实现一个是单线程的，一个是多线程的。
 *
 * @author jimichan
 */
public interface PerceptronTrainer {

    /**
     * 训练一个序列。在一个语料库训练中，改方法需要被调用很多次。
     *
     * @param source 语料库
     */
    PerceptronModel trainSequenceSource(Iterable<SequenceLabel> source);

}
