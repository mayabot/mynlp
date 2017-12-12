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


import java.util.LinkedList;

/**
 * 静态CRF分词模型.
 * crf模型的数据源头是2014年人民日报语料（该语料是机器产生的）
 * 训练出来的
 *
 * @author hankcs
 */
public final class CRFSegmentModel extends CRFModel {

    private int idM;
    private int idE;
    private int idS;

    /**
     * 不允许构造空白实例
     */
    CRFSegmentModel() {
    }


    /**
     * 初始化几个常量
     */
    private void initTagSet() {
        idM = this.getTagId("M");
        idE = this.getTagId("E");
        idS = this.getTagId("S");
    }

    @Override
    protected void onLoadTxtFinished() {
        super.onLoadTxtFinished();
        initTagSet();
    }

    @Override
    public void tag(Table table) {
        int size = table.size();
        if (size == 1) {
            table.setLast(0, "S");
            return;
        }
        int xxx = 0;
        double[][] net = new double[size][4];
        for (int i = 0; i < size; ++i) {
            LinkedList<double[]> scoreList = computeScoreList(table, i);
            for (int tag = 0; tag < 4; ++tag) {
                net[i][tag] = computeScore(scoreList, tag);
            }
        }
        net[0][idM] = -1000.0;  // 第一个字不可能是M或E
        net[0][idE] = -1000.0;
        int[][] from = new int[size][4];
        for (int i = 1; i < size; ++i) {
            for (int now = 0; now < 4; ++now) {
                double maxScore = -1e10;
                for (int pre = 0; pre < 4; ++pre) {
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
        int maxTag = net[size - 1][idS] > net[size - 1][idE] ? idS : idE;
        table.setLast(size - 1, id2tag[maxTag]);
        maxTag = from[size - 1][maxTag];
        for (int i = size - 2; i > 0; --i) {
            table.setLast(i, id2tag[maxTag]);
            maxTag = from[i][maxTag];
        }
        table.setLast(0, id2tag[maxTag]);
    }
}
