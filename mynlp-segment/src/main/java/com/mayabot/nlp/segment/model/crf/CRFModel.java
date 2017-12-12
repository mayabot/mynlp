/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.mayabot.nlp.segment.model.crf;

import com.mayabot.nlp.collection.Trie;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author hankcs
 */
public class CRFModel {
    /**
     * 标签和id的相互转换
     */
    Map<String, Integer> tag2id;
    /**
     * id转标签
     */
    protected String[] id2tag;
    /**
     * 特征函数
     */
    Trie<FeatureFunction> featureFunctionTrie;
    /**
     * 特征模板
     */
    List<FeatureTemplate> featureTemplateList;
    /**
     * tag的二元转移矩阵，适用于BiGram Feature
     */
    protected double[][] matrix;

    /**
     * 以指定的trie树结构储存内部特征函数
     */
    public CRFModel() {
    }

    protected void onLoadTxtFinished() {
        // do nothing
    }

    public void setFeatureFunctionTrie(Trie<FeatureFunction> featureFunctionTrie) {
        this.featureFunctionTrie = featureFunctionTrie;
    }

    /**
     * 维特比后向算法标注
     *
     * @param table
     */
    public void tag(Table table) {
        int size = table.size();
        if (size == 0) return;
        int tagSize = id2tag.length;
        double[][] net = new double[size][tagSize];
        for (int i = 0; i < size; ++i) {
            LinkedList<double[]> scoreList = computeScoreList(table, i);
            for (int tag = 0; tag < tagSize; ++tag) {
                net[i][tag] = computeScore(scoreList, tag);
            }
        }

        if (size == 1) {
            double maxScore = -1e10;
            int bestTag = 0;
            for (int tag = 0; tag < net[0].length; ++tag) {
                if (net[0][tag] > maxScore) {
                    maxScore = net[0][tag];
                    bestTag = tag;
                }
            }
            table.setLast(0, id2tag[bestTag]);
            return;
        }

        int[][] from = new int[size][tagSize];
        for (int i = 1; i < size; ++i) {
            for (int now = 0; now < tagSize; ++now) {
                double maxScore = -1e10;
                for (int pre = 0; pre < tagSize; ++pre) {
                    double score = net[i - 1][pre] + matrix[pre][now] + net[i][now];
                    if (score > maxScore) {
                        maxScore = score;
                        from[i][now] = pre;
                    }
                }
                net[i][now] = maxScore;
            }
        }
        // 反向回溯最佳路径
        double maxScore = -1e10;
        int maxTag = 0;
        for (int tag = 0; tag < net[size - 1].length; ++tag) {
            if (net[size - 1][tag] > maxScore) {
                maxScore = net[size - 1][tag];
                maxTag = tag;
            }
        }

        table.setLast(size - 1, id2tag[maxTag]);
        maxTag = from[size - 1][maxTag];
        for (int i = size - 2; i > 0; --i) {
            table.setLast(i, id2tag[maxTag]);
            maxTag = from[i][maxTag];
        }
        table.setLast(0, id2tag[maxTag]);
    }

    /**
     * 根据特征函数计算输出
     *
     * @param table
     * @param current
     * @return
     */
    protected LinkedList<double[]> computeScoreList(Table table, int current) {
        LinkedList<double[]> scoreList = new LinkedList<double[]>();
        for (FeatureTemplate featureTemplate : featureTemplateList) {
            char[] o = featureTemplate.generateParameter(table, current);
            FeatureFunction featureFunction = featureFunctionTrie.get(o);
            if (featureFunction == null) continue;
            scoreList.add(featureFunction.w);
        }

        return scoreList;
    }

    /**
     * 给一系列特征函数结合tag打分
     *
     * @param scoreList
     * @param tag
     * @return
     */
    protected static double computeScore(LinkedList<double[]> scoreList, int tag) {
        double score = 0;
        for (double[] w : scoreList) {
            score += w[tag];
        }
        return score;
    }

    /**
     * 获取某个tag的ID
     *
     * @param tag
     * @return
     */
    public Integer getTagId(String tag) {
        return tag2id.get(tag);
    }
}
