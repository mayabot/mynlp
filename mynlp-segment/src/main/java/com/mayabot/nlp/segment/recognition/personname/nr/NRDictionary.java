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
package com.mayabot.nlp.segment.recognition.personname.nr;

import com.alibaba.fastjson.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.segment.common.EnumFreqPair;
import com.mayabot.nlp.segment.dictionary.CommonDictionary;
import com.mayabot.nlp.segment.recognition.personname.NRTag;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;
import java.util.Map;

/**
 * 一个好用的人名词典
 *
 * @author hankcs
 */
@Singleton
public class NRDictionary extends CommonDictionary<EnumFreqPair<NRTag>> {

    @Inject
    public NRDictionary(Environment environment) throws Exception {
        super(environment);
    }

    @Override
    public EnumFreqPair<NRTag> parseLine(List<String> pars) {
        return EnumFreqPair.create(pars, NRTag::valueOf);
    }

    @Override
    public String dicFilePath() {
        return "inner://dictionary/person/nr.txt";
    }

    @Override
    protected void writeItem(EnumFreqPair<NRTag> a, DataOutput out) {
        a.writeItem(out);
    }

    final static TypeReference<Map<NRTag, Integer>> typeReference = new TypeReference<Map<NRTag, Integer>>() {
    };

    @Override
    protected EnumFreqPair<NRTag> readItem(DataInput in) {
        EnumFreqPair<NRTag> pair = new EnumFreqPair<>();

        pair.readItem(in, typeReference);

        return pair;
    }

}
