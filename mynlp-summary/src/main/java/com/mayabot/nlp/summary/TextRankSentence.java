
package com.mayabot.nlp.summary;

import java.util.*;

/**
 * TextRank 自动摘要
 *
 * @author hankcs
 */
class TextRankSentence {
    /**
     * 阻尼系数，一般取值为0.85
     */
    final static double d = 0.85;
    /**
     * 最大迭代次数
     */
    final static int max_iter = 200;
    final static double min_diff = 0.001;


    /**
     * 文档句子的个数
     */
    int D;
    /**
     * 拆分为[句子[单词]]形式的文档
     */
    List<List<String>> docs;
    /**
     * 排序后的最终结果 score <-> index
     */
    TreeMap<Double, Integer> top;

    /**
     * 句子和其他句子的相关程度
     */
    double[][] weight;
    /**
     * 该句子和其他句子相关程度之和
     */
    double[] weight_sum;
    /**
     * 迭代之后收敛的权重
     */
    double[] vertex;

    /**
     * BM25相似度
     */
    BM25 bm25;

    public TextRankSentence(List<List<String>> docs) {
        this.docs = docs;
        bm25 = new BM25(docs);
        D = docs.size();
        weight = new double[D][D];
        weight_sum = new double[D];
        vertex = new double[D];
        top = new TreeMap<>(Collections.reverseOrder());
        solve();
    }

    private void solve() {
        int cnt = 0;
        for (List<String> sentence : docs) {
            double[] scores = bm25.simAll(sentence);
            weight[cnt] = scores;
            // 减掉自己，自己跟自己肯定最相似
            weight_sum[cnt] = sum(scores) - scores[cnt];
            vertex[cnt] = 1.0;
            ++cnt;
        }
        for (int _ = 0; _ < max_iter; ++_) {
            double[] m = new double[D];
            double max_diff = 0;
            for (int i = 0; i < D; ++i) {
                m[i] = 1 - d;
                for (int j = 0; j < D; ++j) {
                    if (j == i || weight_sum[j] == 0) {
                        continue;
                    }
                    m[i] += (d * weight[j][i] / weight_sum[j] * vertex[j]);
                }
                double diff = Math.abs(m[i] - vertex[i]);
                if (diff > max_diff) {
                    max_diff = diff;
                }
            }
            vertex = m;
            if (max_diff <= min_diff) {
                break;
            }
        }
        // 我们来排个序吧
        for (int i = 0; i < D; ++i) {
            top.put(vertex[i], i);
        }
    }

    /**
     * 获取前几个关键句子
     *
     * @param size 要几个
     * @return 关键句子的下标
     */
    public int[] getTopSentence(int size) {
        Collection<Integer> values = top.values();
        size = Math.min(size, values.size());
        int[] indexArray = new int[size];
        Iterator<Integer> it = values.iterator();
        for (int i = 0; i < size; ++i) {
            indexArray[i] = it.next();
        }
        return indexArray;
    }

    /**
     * 简单的求和
     *
     * @param array
     * @return
     */
    private double sum(double[] array) {
        double total = 0;
        for (double v : array) {
            total += v;
        }
        return total;
    }


}
