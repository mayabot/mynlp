package com.mayabot.nlp.perceptron;

public interface FeatureExtractor<T> {
     int[] featureExtract(T[] sentence, int position, FeatureMap featureMap);
}
