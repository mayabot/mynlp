package com.mayabot.nlp.perceptron;

public interface FeatureExtractor<T> {
     int[] extractFeature(T[] sentence, int position, FeatureMap featureMap);
}
