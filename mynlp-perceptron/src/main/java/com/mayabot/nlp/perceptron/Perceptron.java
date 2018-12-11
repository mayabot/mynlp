package com.mayabot.nlp.perceptron;

import com.carrotsearch.hppc.IntArrayList;

import java.io.File;
import java.util.List;

/**
 * 感知机模型。
 *
 * 这个感知机面向的是向量进行计算的，提供最最底层的实现。
 *
 * FeatureSet中保存了所有的特征，每个特征都有自己的数组下标。
 * 系统中存在一共N个特征。
 *
 * 感知机处理的向量是一个特征向量的长度是N，但是是觉得稀疏的向量，只有
 * 某几位是1，其他的为0，所以使用有限的int[]来保存为1的向量的下标。
 *
 * decode中使用了int[]中的，最后一位是留个转移特征使用的。
 *
 * 所以抽取特征向量的时候需要注意。
 * //// 最后一列留给转移特征
 * result.add(0)
 *
 * @author jimichan
 */
public interface Perceptron {
    /**
     * 保存感知机模型实例
     *
     * @param dir File
     */
    void save(File dir);


    /**
     * 特征集合
     *
     * @return 特征集合
     */
    FeatureSet featureSet();


    void makeSureParameter(int featureId);

    /**
     * 在线学习,更新
     *
     * @param data 样例
     */
    void update(TrainSample data);

    /**
     * 压缩
     * @param ratio 压缩比，0.1表示压缩去掉10%的特征
     * @param threshold 特征最小得分
     */
    void compress(double ratio, double threshold);

    /**
     * 解码
     *
     * @param featureSequence FeatureSequence 为一个浓缩的特征向量，最后一位是留给转移特征。
     */
    void decode(List<IntArrayList> featureSequence, int[] guessLabel);

    /**
     * 解码
     *
     * @param featureSequence FeatureSequence 稀疏特征向量的简短表示，最后一位是留给转移特征。
     * @return label对应的ID数组
     */
    default int[] decode(List<IntArrayList> featureSequence) {
        int[] result = new int[featureSequence.size()];

        if (result.length == 0) {
            return result;
        }

        decode(featureSequence, result);

        return result;
    }
}
