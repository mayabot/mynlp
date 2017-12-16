package com.mayabot.nlp.segment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class WordnetTokenizerBuilder {

    private String bestPath;
    private String wordnetIniter;
    private List<WordnetTokenizerFactory.PipelineItem> pipelineItem = Lists.newArrayList();

    private WordnetTokenizerBuilder(){

    }

    public String getBestPath() {
        return bestPath;
    }

    public String getWordnetIniter() {
        return wordnetIniter;
    }

    public List<WordnetTokenizerFactory.PipelineItem> getPipelineItem() {
        return pipelineItem;
    }

    public void setPipelineItem(List<WordnetTokenizerFactory.PipelineItem> pipelineItem) {
        this.pipelineItem = pipelineItem;
    }

    public static WordnetTokenizerBuilder create(){
        return new WordnetTokenizerBuilder();
    }

    public WordnetTokenizerBuilder setBestPath(String bestPath) {
        this.bestPath = bestPath;
        return this;
    }

    public WordnetTokenizerBuilder setWordnetIniter(String wordnetIniter) {
        this.wordnetIniter = wordnetIniter;
        return this;
    }

    public WordnetTokenizerBuilder add(String type){
        pipelineItem.add(new WordnetTokenizerFactory.PipelineItem(type));
        return this;
    }

    public WordnetTokenizerBuilder add(String type, Map<String,Object> config){
        pipelineItem.add(new WordnetTokenizerFactory.PipelineItem(type,config));
        return this;
    }

    public WordnetTokenizerBuilder addOptimizeNetWordPathProcessor(List<String> plist){
        HashMap<String , Object> map = Maps.newHashMap();
        map.put("list", plist);
        pipelineItem.add(new WordnetTokenizerFactory.PipelineItem("optimizeNet",map));
        return this;
    }


    public WordnetTokenizerBuilder get() {
        return this;
    }
}
