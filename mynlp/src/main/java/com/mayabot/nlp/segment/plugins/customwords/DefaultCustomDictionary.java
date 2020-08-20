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

package com.mayabot.nlp.segment.plugins.customwords;

import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieStringIntMap;
import com.mayabot.nlp.common.injector.Singleton;
import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.common.resources.NlpResource;
import com.mayabot.nlp.common.resources.UseLines;
import com.mayabot.nlp.common.utils.CharNormUtils;
import com.mayabot.nlp.common.utils.CharSourceLineReader;

import java.util.List;
import java.util.TreeMap;

import static com.mayabot.nlp.MynlpConfigs.dictPathSetting;

/**
 * 用户自定义词典
 *
 * @author jimichan
 */
@Singleton
public class DefaultCustomDictionary implements CustomDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(DefaultCustomDictionary.class);

    private DoubleArrayTrieStringIntMap dat;

    private List<String> resourceUrls;

    private boolean isNormalization = false;


    public DefaultCustomDictionary(MynlpEnv env) throws Exception {

        List<String> resourceUrls = env.getAsList(dictPathSetting);

        if (resourceUrls == null || resourceUrls.isEmpty()) {
            return;
        }

        this.resourceUrls = resourceUrls;


        TreeMap<String, Integer> map = new TreeMap<>();

        for (String url : resourceUrls) {
            NlpResource resource = env.tryLoadResource(url);

            if (resource == null) {
                logger.warn("miss resource "+url);
                continue;
            }

            try (CharSourceLineReader reader = UseLines.lineReader(resource.inputStream())) {
                while (reader.hasNext()) {
                    String line = reader.next();

                    String[] params = line.split("\\s");

                    if (isNormalization) {
                        params[0] = normalizationString(params[0]);
                    }

                    map.put(params[0], 1000);
                }
            }
        }


        if (map.isEmpty()) {
            return;
        }

        dat = new DoubleArrayTrieStringIntMap(map);
    }

    private String normalizationString(String text) {
        return CharNormUtils.convert(text);
    }

    @Override
    public DoubleArrayTrieStringIntMap getTrie() {
        return dat;
    }
}
