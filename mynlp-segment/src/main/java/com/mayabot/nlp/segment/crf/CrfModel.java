/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mayabot.nlp.segment.crf;

import org.trie4j.louds.MapTailLOUDSTrie;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * CrfModel是把CRF模型文件在内存中的表示。
 * 其中特征函数的存储实现和HANLP不同，这里采用org.trie4j.MapTrie。兼顾内存占用和加载时间和查询时间.
 *
 * @author hankcs
 * @author jimichan
 */
public class CrfModel {
    /**
     * 标签和id的相互转换
     */
    protected Map<String, Integer> tag2id;
    /**
     * id转标签
     */
    protected String[] id2tag;
    /**
     * 特征函数
     */
    protected MapTailLOUDSTrie<FeatureFunction> featureFunctionTrie;

    /**
     * 特征模板
     */
    protected List<FeatureTemplate> featureTemplateList;
    /**
     * tag的二元转移矩阵，适用于BiGram Feature
     */
    protected double[][] matrix;

    /**
     * 以指定的trie树结构储存内部特征函数
     */
    public CrfModel() {
    }

    public void write(ObjectOutput out) throws IOException {
        out.writeObject(tag2id);
        out.writeObject(id2tag);
        out.writeObject(matrix);
        out.writeObject(featureTemplateList);
        featureFunctionTrie.writeExternal(out);
    }

    public void load(ObjectInput input) throws IOException, ClassNotFoundException {
        tag2id = (Map<String, Integer>) input.readObject();
        id2tag = (String[]) input.readObject();
        matrix = (double[][]) input.readObject();
        featureTemplateList = (List<FeatureTemplate>) input.readObject();

        featureFunctionTrie = new MapTailLOUDSTrie<>();
        featureFunctionTrie.readExternal(input);
    }


    protected void onLoadTxtFinished() {
        // do nothing
    }

    public void setFeatureFunctionTrie(MapTailLOUDSTrie<FeatureFunction> featureFunctionTrie) {
        this.featureFunctionTrie = featureFunctionTrie;
    }

    /**
     * 维特比后向算法标注
     *
     * @param table
     */
    public void tag(Table table) {
        int size = table.size();
        if (size == 0) {
            return;
        }
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
            if (featureFunction == null) {
                continue;
            }
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
