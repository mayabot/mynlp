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
package com.mayabot.nlp.segment.tokenizer.recognition.personname.nr;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.common.EnumFreqPair;
import com.mayabot.nlp.segment.dictionary.CommonDictionary;
import com.mayabot.nlp.segment.tokenizer.recognition.personname.NRTag;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;

/**
 * 一个好用的人名词典
 *
 * @author hankcs
 * @author jimichan
 */
@Singleton
public class NRDictionary extends CommonDictionary<EnumFreqPair<NRTag>> {

    @Inject
    public NRDictionary(MynlpEnv mynlp) throws Exception {
        super(mynlp);
    }

    @Override
    public EnumFreqPair<NRTag> parseLine(List<String> pars) {
        return EnumFreqPair.create(pars, NRTag::valueOf);
    }

    @Override
    public String dicFilePath() {
        return "dictionary/person/nr.txt";
    }

    @Override
    protected void writeItem(EnumFreqPair<NRTag> a, DataOutput out) {
        a.writeItem(out);
    }


    @Override
    protected EnumFreqPair<NRTag> readItem(DataInput in) {
        EnumFreqPair<NRTag> pair = new EnumFreqPair<>();

        pair.readItem(in, NRTag::valueOf);

        return pair;
    }

}
