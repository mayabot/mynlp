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


import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import org.trie4j.louds.MapTailLOUDSTrie;
import org.trie4j.patricia.MapPatriciaTrie;

import java.util.*;

/**
 * 静态CRF分词模型.
 * crf模型的数据源头是2014年人民日报语料（该语料是机器产生的）
 * 训练出来的
 *
 * @author hankcs
 * @author jimichan
 */
public final class CrfSegmentModel extends CrfModel {

    protected static InternalLogger logger = InternalLoggerFactory.getInstance(CrfSegmentModel.class);

    private int idM;
    private int idE;
    private int idS;

    /**
     * 不允许构造空白实例
     */
    CrfSegmentModel() {
    }

    public static CrfSegmentModel loadFromCrfPlusText(Iterator<String> txtReader) {
        CrfSegmentModel model = new CrfSegmentModel();


        if (!txtReader.hasNext()) {
            return null;
        }

        logger.info(txtReader.next());   // verson

        logger.info(txtReader.next());   // cost-factor
        int maxid = Integer.parseInt(txtReader.next().substring("maxid:".length()).trim());
        logger.info(txtReader.next());   // xsize

        txtReader.next();    // blank


        String line;
        int id = 0;
        model.tag2id = new HashMap<String, Integer>();
        while ((line = txtReader.next()).length() != 0) {
            model.tag2id.put(line, id);
            ++id;
        }
        model.id2tag = new String[model.tag2id.size()];
        final int size = model.id2tag.length;
        for (Map.Entry<String, Integer> entry : model.tag2id.entrySet()) {
            model.id2tag[entry.getValue()] = entry.getKey();
        }
        MapPatriciaTrie<FeatureFunction> featureFunctionMap = new MapPatriciaTrie<>();

        // 读取权值的时候用
        List<FeatureFunction> featureFunctionList = new LinkedList<FeatureFunction>();
        model.featureTemplateList = new LinkedList<>();
        while ((line = txtReader.next()).length() != 0) {
            if (!"B".equals(line)) {
                FeatureTemplate featureTemplate = FeatureTemplate.create(line);
                model.featureTemplateList.add(featureTemplate);
            } else {
                model.matrix = new double[size][size];
            }
        }

        if (model.matrix != null) {
            txtReader.next();    // 0 B
        }

        while (txtReader.hasNext()) {
            line = txtReader.next();
            if (line.isEmpty()) {
                break;
            }
            String[] args = line.split(" ", 2);
            char[] charArray = args[1].toCharArray();
            FeatureFunction featureFunction = new FeatureFunction(charArray, size);
            featureFunctionMap.insert(args[1], featureFunction);
            featureFunctionList.add(featureFunction);
        }

        if (model.matrix != null) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    model.matrix[i][j] = Double.parseDouble(txtReader.next());
                }
            }
        }

        for (FeatureFunction featureFunction : featureFunctionList) {
            for (int i = 0; i < size; i++) {
                featureFunction.w[i] = Double.parseDouble(txtReader.next());
            }
        }

        if (txtReader.hasNext()) {
            logger.warn("CRF 文本读取有残留，可能会出问题！");
        }

        logger.info("开始构建trie树 featureFunctionMap.size = " +featureFunctionMap.size());

        //这里采用MapTailLOUDSTrie 构建速度快。查询速度稍微慢。
        //TODO 以后提供两张二进制格式。一种是DoubleArray的，内存大，速度快。一种是LOUDSTrie，高压缩比，性能稍微慢。
        //一种是LOUDSTrie适合测试或者对性能不要求不苛刻的大部分场景下使用。
        //或者另外一个办法MapTailLOUDSTrie提供前置缓存，说不定会大大提供效率

        MapTailLOUDSTrie<FeatureFunction> trie = new MapTailLOUDSTrie(featureFunctionMap);
        model.setFeatureFunctionTrie(trie);

        model.onLoadTxtFinished();

        logger.info("完成构建trie树 feature size " + featureFunctionMap.size());

        return model;

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
        // 第一个字不可能是M或E
        net[0][idM] = -1000.0;
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
