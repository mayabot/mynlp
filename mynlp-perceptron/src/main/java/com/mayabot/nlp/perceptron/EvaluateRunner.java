package com.mayabot.nlp.perceptron;

/**
 * 感知机评估逻辑
 */
public interface EvaluateRunner {
    void run(int iter, Perceptron perceptron);
}
