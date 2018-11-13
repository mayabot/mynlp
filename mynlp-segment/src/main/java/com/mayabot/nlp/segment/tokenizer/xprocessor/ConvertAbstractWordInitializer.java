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

package com.mayabot.nlp.segment.tokenizer.xprocessor;

import com.google.inject.Inject;
import com.mayabot.nlp.segment.WordnetInitializer;
import com.mayabot.nlp.segment.common.BaseMynlpComponent;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.segment.wordnet.Wordnet;

/**
 * 核心词典查询后，或计算后，需要根据词性，来弥补一些等效词属性或者词性修正。
 * <p>
 * 后面有些识别器需要这些设置的属性
 * @author jimichan
 */
public class ConvertAbstractWordInitializer extends BaseMynlpComponent implements WordnetInitializer {

    private final CoreDictionary coreDictionary;

    @Inject
    public ConvertAbstractWordInitializer(CoreDictionary coreDictionary) {
        this.coreDictionary = coreDictionary;
        this.setOrder(ORDER_LASTEST);
    }

    @Override
    public void fill(Wordnet wordnet) {
        wordnet.accessAllVertex(this::process);
    }

    private void process(Vertex v) {

        if (v.wordID < 0) {
            // CRF分词结果后，没有wordID
            // 或者其他情况
            int wordID = coreDictionary.getWordID(v.realWord());
            if (wordID >= 0) {
                NatureAttribute na = coreDictionary.get(wordID);
                if (na != null) {
                    v.setWordInfo(wordID, na);
                }
            }
        }

        if (v.natureAttribute == null) {
            return;
        }

        if (v.natureAttribute.size() == 1) {
            String nature = v.natureAttribute.one().getKey().name();
            switch (nature) {
                case "nr":
                case "nr1":
                case "nr2":
                case "nrf":
                case "nrj": {
                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_PEOPLE)
                            .setWordID(coreDictionary.NR_WORD_ID);
                    return;
                }
                case "ns":
                case "nsf": {
                    // 在地名识别的时候,希望类似"河镇"的词语保持自己的词性,而不是未##地的词性
                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_PLACE)
                            .setWordID(coreDictionary.NS_WORD_ID);
                    return;
                }
                //case nz:
                case "nx": {
                    //FIXME 这里的NX不是人名
                    v.setWordID(coreDictionary.NX_WORD_ID)
                            .setAbstractWordIfEmpty(CoreDictionary.TAG_PEOPLE);
                    v.natureAttribute = coreDictionary.get(coreDictionary.NX_WORD_ID);

                    return;
                }
                case "nt":
                case "ntc":
                case "ntcf":
                case "ntcb":
                case "ntch":
                case "nto":
                case "ntu":
                case "nts":
                case "nth":
                case "nit": {
                    v.setWordID(coreDictionary.NT_WORD_ID);
                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_GROUP);
                    return;
                }
                case "m":
                case "mq": {
                    v.setWordID(coreDictionary.M_WORD_ID);
                    v.natureAttribute = coreDictionary.get(coreDictionary.M_WORD_ID);

                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_NUMBER);
                    return;
                }
                case "x": {
                    v.setWordID(coreDictionary.X_WORD_ID);
                    v.natureAttribute = coreDictionary.get(coreDictionary.X_WORD_ID);
                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_CLUSTER);

                    return;
                }
                case "t": {
                    v.setWordID(coreDictionary.T_WORD_ID);
                    v.natureAttribute = coreDictionary.get(coreDictionary.T_WORD_ID);
                    v.setAbstractWordIfEmpty(CoreDictionary.TAG_TIME);

                    return;
                }

            }
        }
    }


}
