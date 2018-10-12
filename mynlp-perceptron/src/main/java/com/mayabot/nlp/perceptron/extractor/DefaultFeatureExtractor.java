package com.mayabot.nlp.perceptron.extractor;

import com.mayabot.nlp.perceptron.FeatureExtractor;
import com.mayabot.nlp.perceptron.FeatureMap;

import java.util.List;

abstract class DefaultFeatureExtractor<T> implements FeatureExtractor<T> {
    void addFeatureThenClear(StringBuilder rawFeature, List<Integer> featureVector, FeatureMap featureMap) {
        int id = featureMap.idOf(rawFeature.toString());
        if (id != -1) {
            featureVector.add(id);
        }
        rawFeature.setLength(0);
    }

    int[] toFeatureArray(List<Integer> featureVector) {
        int[] featureArray = new int[featureVector.size() + 1];   // 最后一列留给转移特征
        int index = -1;
        for (Integer feature : featureVector) {
            featureArray[++index] = feature;
        }

        return featureArray;
    }
}
