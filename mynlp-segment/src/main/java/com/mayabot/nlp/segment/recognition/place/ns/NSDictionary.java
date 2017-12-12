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

package com.mayabot.nlp.segment.recognition.place.ns;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.ResourceLoader;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.collection.ValueSerializer;
import com.mayabot.nlp.segment.corpus.dictionary.item.EnumFreqPair;
import com.mayabot.nlp.segment.corpus.tag.NSTag;
import com.mayabot.nlp.segment.dictionary.CommonDictionary;

import java.io.File;
import java.util.List;

@Singleton
public class NSDictionary extends CommonDictionary<EnumFreqPair<NSTag>> {

    final static String file = "place" + File.separator + "ns.txt";

    @Inject
    public NSDictionary(Settings setting, ResourceLoader resourceLoader, Environment environment) {
        super( setting, resourceLoader, environment);
    }

    @Override
    public EnumFreqPair<NSTag> parseLine(List<String> pars) {
        return EnumFreqPair.create(pars, NSTag::valueOf);
    }

    @Override
    public String dicFilePath() {
        return file;
    }

    @Override
    public ValueSerializer<EnumFreqPair<NSTag>> valueSerializer() {
        return ValueSerializer.jdk();
    }
}
