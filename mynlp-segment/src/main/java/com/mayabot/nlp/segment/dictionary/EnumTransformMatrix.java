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
package com.mayabot.nlp.segment.dictionary;

import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.collection.TransformMatrix;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.IOException;

/**
 * 转移矩阵词典
 *
 * @param <E> 标签的枚举类型
 */
public class EnumTransformMatrix<E extends Enum<E>> {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(EnumTransformMatrix.class);

    private TransformMatrix transformMatrix;

    public EnumTransformMatrix(String file, ResourceLoader resourceLoader) throws IOException {
        transformMatrix = new TransformMatrix();
        long t1 = System.currentTimeMillis();
        transformMatrix.load(resourceLoader.loadDictionary(file));
        long t2 = System.currentTimeMillis();
        logger.info("加载核" + file + "转移矩阵" + file + "成功，耗时：" + (t2 - t1) + " ms");
    }

    public int getFrequency(E from, E to) {
        return transformMatrix.getFrequency(from.name(), to.name());
    }

    public long getTotalFrequency(E from) {
        return transformMatrix.getTotalFrequency(from.name());
    }

    public long getTotalFrequency() {
        return transformMatrix.getTotalFrequency();
    }

    public String toString() {
        return transformMatrix.toString();
    }

    /**
     * 获取转移概率
     *
     * @param from
     * @param to
     * @return
     */
    public double getTP(E from, E to) {
        Double x = transformMatrix.getTP(from.name(), to.name());
        if (x != null) {
            return x.doubleValue();
        } else {
            return 0;
        }
    }

}
