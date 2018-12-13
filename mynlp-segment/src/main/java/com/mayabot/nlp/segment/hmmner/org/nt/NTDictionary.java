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

package com.mayabot.nlp.segment.hmmner.org.nt;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.common.EnumFreqPair;
import com.mayabot.nlp.segment.hmmner.CommonDictionary;
import com.mayabot.nlp.segment.hmmner.org.NTTag;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;

/**
 * @author jimichan
 */
@Singleton
public class NTDictionary extends CommonDictionary<EnumFreqPair<NTTag>> {

    @Inject
    public NTDictionary(MynlpEnv mynlp) throws Exception {
        super(mynlp);
    }

    @Override
    public EnumFreqPair<NTTag> parseLine(List<String> pars) {
        return EnumFreqPair.create(pars, NTTag::valueOf);
    }

    @Override
    public String dicFilePath() {
        return "organization/nt.txt";
    }

    @Override
    protected void writeItem(EnumFreqPair<NTTag> a, DataOutput out) {
        a.writeItem(out);
    }

    @Override
    protected EnumFreqPair<NTTag> readItem(DataInput in) {
        EnumFreqPair<NTTag> pair = new EnumFreqPair<>();

        pair.readItem(in, NTTag::valueOf);

        return pair;
    }
}