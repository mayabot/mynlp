package com.mayabot.nlp.segment;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.Settings;
import com.mayabot.nlp.segment.tokenizer.PipelineDefine;
import com.mayabot.nlp.segment.tokenizer.WordnetTokenizerFactory;
import com.mayabot.nlp.segment.wordnet.ViterbiBestPathComputer;

import java.util.Set;
import java.util.stream.Collectors;

import static com.mayabot.nlp.segment.ComponentRegistry.WORDNET_INITER_CORE;
import static com.mayabot.nlp.segment.ComponentRegistry.WORDNET_INITER_CRF;

/**
 * 规范Api来设置settings，客户端用户同步IDE提示即可访问所有setting的配置项。
 * 当用户自己扩展了分词组件，对应的只需要继承TokenizerBuilder进行配置项扩展。
 *
 * @author jimichan
 */
public class MynlpTokenizerBuilder {

    private Settings settings = Settings.createEmpty();

    private Set<String> disableNames = Sets.newHashSet();

    private PipelineDefine pipelineDefine = PipelineDefine.defaultPipeline;

    public static final MynlpTokenizerBuilder builder() {
        return new MynlpTokenizerBuilder();
    }

    public MynlpTokenizerBuilder wordnetIniter(String name) {
        settings.put(WordnetTokenizerFactory.TOKENIZER_INITER, name);
        return this;
    }

    public MynlpTokenizerBuilder coreWordnetIniter() {
        return wordnetIniter(WORDNET_INITER_CORE);
    }

    public MynlpTokenizerBuilder crfWordnetIniter() {
        return wordnetIniter(WORDNET_INITER_CRF);
    }

    public MynlpTokenizerBuilder bestPath(String pathName) {
        settings.put(WordnetTokenizerFactory.TOKENIZER_BESTPATH, pathName);
        return this;
    }

    public MynlpTokenizerBuilder viterbiBestPath() {
        return bestPath(ViterbiBestPathComputer.name);
    }

    public MynlpTokenizerBuilder pipeline(PipelineDefine pipeLine) {
        this.pipelineDefine = pipelineDefine;
        return this;
    }

    public MynlpTokenizerBuilder disable(ComponentNames... name) {
        disableNames.addAll(Lists.newArrayList(name).stream().map(x -> x.name()).collect(Collectors.toList()));
        return this;
    }

    public MynlpTokenizerBuilder disable(String... name) {
        disableNames.addAll(Lists.newArrayList(name));
        return this;
    }

    public MynlpTokenizerBuilder set(String key, String value) {
        settings.put(key, value);
        return this;
    }

    public MynlpTokenizerBuilder set(Setting key, String value) {
        settings.put(key, value);
        return this;
    }

    private MynlpTokenizer instance = null;

    public MynlpTokenizer build() {
        if (instance == null) {
            settings.put("pipeline.disable", Joiner.on(',').join(disableNames));
            instance = WordnetTokenizerFactory.instance().build(pipelineDefine, settings);
        }
        return instance;
    }
}