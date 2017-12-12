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

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *{
 *     "path:"xxx"
 *}
 *
 *
 */
public class TokenizerConfigure {

    private String bestPathComputer = "viterbi";

    //initialize
    private List<String> wordnetIniter = Lists.newArrayList();

    private List<Object> processPipeline = Lists.newArrayList();

    public static class OptimizeWordnetProcessorNames extends ArrayList<String>{

    }

    public TokenizerConfigure addLastPipeline(String name) {
        processPipeline.add(name);
        return this;
    }

    public TokenizerConfigure addLastPipelines(Object... names) {
        for (Object name : names) {
            if (name instanceof String) {
                addLastPipeline(((String) name));
            }else if(name instanceof OptimizeWordnetProcessorNames){
                addLastPipeline(((OptimizeWordnetProcessorNames) name));
            }
        }
        return this;
    }



    public TokenizerConfigure addLastPipeline(OptimizeWordnetProcessorNames name) {
        processPipeline.add(name);
        return this;
    }

    public OptimizeWordnetProcessorNames optimizeWordnetProcessorNames(String... names) {
        OptimizeWordnetProcessorNames x = new OptimizeWordnetProcessorNames();
        x.addAll(Lists.newArrayList(names));
        return x;
    }


    public String getBestPathComputer() {
        return bestPathComputer;
    }

    public List<String> getWordnetIniter() {
        return wordnetIniter;
    }

    public List<Object> getProcessPipeline() {
        return processPipeline;
    }

    public void setBestPathComputer(String bestPathComputer) {
        this.bestPathComputer = bestPathComputer;
    }

    public void setWordnetIniter(String... wordnetIniter) {
        this.wordnetIniter = Lists.newArrayList(wordnetIniter);
    }

    public static TokenizerConfigure fromJson(String json){
        return null;
    }
}
