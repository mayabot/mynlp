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

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;
import com.mayabot.nlp.collection.dat.DoubleArrayTrieBuilder;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.utils.FileLineReader;

import java.io.IOException;
import java.util.*;

@Singleton
public class CRFModelComponent {

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    private final Settings settings;
    private final ResourceLoader resourceLoader;

    @Inject
    public CRFModelComponent( Settings settings, ResourceLoader resourceLoader) {
        this.settings = settings;
        this.resourceLoader = resourceLoader;
    }


    CRFSegmentModel crfSegmentModel = null;

    String crfSegmentModelPath = "segment/CRFSegmentModel.txt";

    public CRFSegmentModel load() throws IOException {

        if (crfSegmentModel == null) {
            CRFSegmentModel model = new CRFSegmentModel();
            loadTxt(crfSegmentModelPath, model);
            this.crfSegmentModel = model;
        }

        return crfSegmentModel;
    }

    /**
     * 加载Txt形式的CRF++模型
     *
     * @param path     模型路径
     * @param instance 模型的实例（这里允许用户构造不同的CRFModel来储存最终读取的结果）
     * @return 该模型
     */
    public void loadTxt(String path, CRFModel instance) throws IOException {
        CRFModel CRFModel = instance;

        ByteSource byteSource = resourceLoader.loadModel(path);
        try (FileLineReader lineIterator = new FileLineReader(byteSource.asCharSource(Charsets.UTF_8))) {

            if (!lineIterator.hasNext()) return;

            logger.info(lineIterator.next());   // verson
            logger.info(lineIterator.next());   // cost-factor
            int maxid = Integer.parseInt(lineIterator.next().substring("maxid:".length()).trim());
            logger.info(lineIterator.next());   // xsize

            lineIterator.next();    // blank


            String line;
            int id = 0;
            CRFModel.tag2id = new HashMap<String, Integer>();
            while ((line = lineIterator.next()).length() != 0) {
                CRFModel.tag2id.put(line, id);
                ++id;
            }
            CRFModel.id2tag = new String[CRFModel.tag2id.size()];
            final int size = CRFModel.id2tag.length;
            for (Map.Entry<String, Integer> entry : CRFModel.tag2id.entrySet()) {
                CRFModel.id2tag[entry.getValue()] = entry.getKey();
            }
            TreeMap<String, FeatureFunction> featureFunctionMap = new TreeMap<String, FeatureFunction>();  // 构建trie树的时候用
            List<FeatureFunction> featureFunctionList = new LinkedList<FeatureFunction>(); // 读取权值的时候用
            CRFModel.featureTemplateList = new LinkedList<FeatureTemplate>();
            while ((line = lineIterator.next()).length() != 0) {
                if (!"B".equals(line)) {
                    FeatureTemplate featureTemplate = FeatureTemplate.create(line);
                    CRFModel.featureTemplateList.add(featureTemplate);
                } else {
                    CRFModel.matrix = new double[size][size];
                }
            }

            if (CRFModel.matrix != null) {
                lineIterator.next();    // 0 B
            }

            while (lineIterator.hasNext()) {
                line = lineIterator.next();
                if (line.isEmpty()) {
                    break;
                }
                String[] args = line.split(" ", 2);
                char[] charArray = args[1].toCharArray();
                FeatureFunction featureFunction = new FeatureFunction(charArray, size);
                featureFunctionMap.put(args[1], featureFunction);
                featureFunctionList.add(featureFunction);
            }

            if (CRFModel.matrix != null) {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        CRFModel.matrix[i][j] = Double.parseDouble(lineIterator.next());
                    }
                }
            }

            for (FeatureFunction featureFunction : featureFunctionList) {
                for (int i = 0; i < size; i++) {
                    featureFunction.w[i] = Double.parseDouble(lineIterator.next());
                }
            }
            if (lineIterator.hasNext()) {
                logger.warn("文本读取有残留，可能会出问题！" + path);
            }
            lineIterator.close();
            logger.info("开始构建trie树");
            DoubleArrayTrie<FeatureFunction> trie = new DoubleArrayTrieBuilder().build(featureFunctionMap);
            CRFModel.setFeatureFunctionTrie(trie);

            CRFModel.onLoadTxtFinished();

            logger.info("完成构建trie树 feature size " + featureFunctionMap.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
