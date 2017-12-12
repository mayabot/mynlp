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

package com.mayabot.nlp.segment;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mayabot.nlp.segment.bestpath.ViterbiBestPathComputer;
import com.mayabot.nlp.segment.recognition.org.OrganizationRecognition;
import com.mayabot.nlp.segment.recognition.personname.PersonRecognition;
import com.mayabot.nlp.segment.recognition.place.PlaceRecognition;
import com.mayabot.nlp.segment.wordnetiniter.AtomSegmenter;
import com.mayabot.nlp.segment.wordnetiniter.ConvertAbstractWord;
import com.mayabot.nlp.segment.wordnetiniter.CoreDictionaryOriginalSegment;
import com.mayabot.nlp.segment.wordnetiniter.MultiWordnetInit;
import com.mayabot.nlp.segment.wordnetiniter.crf.CrfOriginalSegment;
import com.mayabot.nlp.segment.xprocessor.*;

/**
 * 切词器模块
 *
 * @author jimichan
 */
public class AnalyzerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NamedComponentRegistry.class).asEagerSingleton();
    }


}
