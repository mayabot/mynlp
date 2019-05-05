package com.mayabot.nlp.perceptron

/**
 * 感知机训练的样本
 * FeatureVector最后多一位是留个转移特征使用
 */
class TrainSample(
        val featureSequence: FeatureVectorSequence,
        val label: IntArray) {

    val size = featureSequence.size

}
