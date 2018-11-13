package com.mayabot.nlp.perceptron;

import java.io.File;
import java.util.List;

/**
 * 感知机模型
 *
 * @author jimichan
 */
public interface PerceptronModel {
    /**
     * 保存感知机模型实例
     *
     * @param file 模型文件的路径
     */
    void save(File file);

    FeatureSet featureSet();

    double decode(List<int[]> featureSequence, int[] guessLabel);

    default int[] decode(List<int[]> featureSequence) {
        int[] result = new int[featureSequence.size()];

        decode(featureSequence, result);

        return result;
    }
}
