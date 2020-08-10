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
package com.mayabot.nlp.segment.plugins.correction;

import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.SettingItem;
import com.mayabot.nlp.algorithm.collection.dat.DoubleArrayTrieMap;
import com.mayabot.nlp.common.injector.Singleton;
import com.mayabot.nlp.common.logging.InternalLogger;
import com.mayabot.nlp.common.logging.InternalLoggerFactory;
import com.mayabot.nlp.common.resources.NlpResource;
import com.mayabot.nlp.common.utils.CharSourceLineReader;

import java.util.List;
import java.util.TreeMap;

import static com.mayabot.nlp.common.resources.UseLines.lineReader;


/**
 * 人工纠错词典
 *
 * <p>
 * 格式 第几套/房
 *
 *
 * <p>
 *
 * @author jimichan
 */
@Singleton
public class DefaultCorrectionDictionary implements CorrectionDictionary {

    static InternalLogger logger = InternalLoggerFactory.getInstance(DefaultCorrectionDictionary.class);

    public final static SettingItem<String> correctionDict = SettingItem.string("correction.dict", "dictionary/correction.txt");


    private DoubleArrayTrieMap<CorrectionWord> doubleArrayTrie;


    @Override
    public DoubleArrayTrieMap<CorrectionWord> getTrie() {
        return doubleArrayTrie;
    }

    public DefaultCorrectionDictionary(MynlpEnv mynlp) throws Exception {

        List<String> resourceUrls = mynlp.getSettings().getAsList(correctionDict);

        if (resourceUrls.isEmpty()) {
            return;
        }

        loadFromRealData(mynlp, resourceUrls);
    }

    public void loadFromRealData(MynlpEnv mynlp, List<String> resourceUrls) throws Exception {
        TreeMap<String, CorrectionWord> map = new TreeMap<>();

        for (String url : resourceUrls) {
            NlpResource resource = mynlp.loadResource(url);
            if (resource != null) {

                try (CharSourceLineReader reader = lineReader(resource.inputStream())) {
                    while (reader.hasNext()) {
                        String line = reader.next();
                        CorrectionWord adjustWord = CorrectionWord.parse(line
                        );
                        map.put(adjustWord.path, adjustWord);
                    }
                }
            }

            if (map.isEmpty()) {
                return;
            }

            this.doubleArrayTrie = new DoubleArrayTrieMap<>(map);
        }

    }
}
