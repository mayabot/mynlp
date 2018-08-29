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

package com.mayabot.nlp.segment.recognition.place.ns;

import com.alibaba.fastjson.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.segment.common.EnumFreqPair;
import com.mayabot.nlp.segment.dictionary.CommonDictionary;
import com.mayabot.nlp.segment.recognition.place.NSTag;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;
import java.util.Map;

@Singleton
public class NSDictionary extends CommonDictionary<EnumFreqPair<NSTag>> {

    @Inject
    public NSDictionary(MynlpEnv mynlp) throws Exception {
        super(mynlp);
    }

    @Override
    public EnumFreqPair<NSTag> parseLine(List<String> pars) {
        return EnumFreqPair.create(pars, NSTag::valueOf);
    }

    @Override
    public String dicFilePath() {
        return "dictionary/place/ns.txt";
    }

    @Override
    protected void writeItem(EnumFreqPair<NSTag> a, DataOutput out) {
        a.writeItem(out);
    }

    final static TypeReference<Map<NSTag, Integer>> typeReference = new TypeReference<Map<NSTag, Integer>>() {
    };

    @Override
    protected EnumFreqPair<NSTag> readItem(DataInput in) {
        EnumFreqPair<NSTag> pair = new EnumFreqPair<>();

        pair.readItem(in, typeReference);

        return pair;
    }
}
