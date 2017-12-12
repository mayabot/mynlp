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
package com.mayabot.nlp.segment.dictionary.core;

import com.google.common.io.ByteSource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.TransformMatrix;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * 核心词典词性转移矩阵
 */
@Singleton
public class CoreDictionaryTransformMatrixDictionary {

    private TransformMatrix transformMatrixDictionary;

    final String file = "core" + File.separator + "CoreNatureDictionary.tr.txt";

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    @Inject
    public CoreDictionaryTransformMatrixDictionary(Settings settings, ResourceLoader resourceLoader) throws IOException {
        transformMatrixDictionary = new TransformMatrix();
        long t1 = System.currentTimeMillis();
        ByteSource source = resourceLoader.loadDictionary(file);
        transformMatrixDictionary.load(source);
        long t2 = System.currentTimeMillis();
        logger.info("加载核心词典词性转移矩阵" + file + "成功，耗时：" + (t2 - t1)
                + " ms");
    }

    public TransformMatrix getTransformMatrixDictionary() {
        return transformMatrixDictionary;
    }
}
