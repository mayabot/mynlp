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

package com.mayabot.nlp.segment.support;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.WordTerm;

import java.io.Reader;
import java.util.Iterator;

public abstract class MynlpAnalyzerWarp implements MynlpAnalyzer {

    protected MynlpAnalyzer myAnalyzer;

    public MynlpAnalyzerWarp(MynlpAnalyzer myAnalyzer) {
        this.myAnalyzer = myAnalyzer;
    }

    @Override
    public MynlpAnalyzer reset(Reader reader) {
        return myAnalyzer.reset(reader);
    }

    @Override
    public Iterator<WordTerm> iterator() {
        return myAnalyzer.iterator();
    }
}
