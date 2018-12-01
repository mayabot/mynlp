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
package com.mayabot.nlp.segment.dictionary.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.common.matrix.TransformMatrix;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;

import java.io.IOException;

/**
 * 核心词典词性转移矩阵
 * @author jimichan
 */
@Singleton
public class CoreDictionaryTransformMatrixDictionary {

    private TransformMatrix transformMatrixDictionary;

    public final String path = "dictionary/CoreDict.tr.txt";

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    @Inject
    public CoreDictionaryTransformMatrixDictionary(MynlpEnv mynlp) throws IOException {
        transformMatrixDictionary = new TransformMatrix();
        long t1 = System.currentTimeMillis();

        NlpResource resource = mynlp.loadResource(path);
        transformMatrixDictionary.load(resource.openInputStream());
        long t2 = System.currentTimeMillis();
        logger.info("加载核心词典词性转移矩阵" + resource + "成功，耗时：" + (t2 - t1) + " ms");
    }

    public TransformMatrix getTransformMatrixDictionary() {
        return transformMatrixDictionary;
    }
}
