package com.mayabot.nlp.segment.plugins.pattern;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

import java.util.regex.Pattern;

/**
 * 基于正则表达式的分词插件
 *
 * @author jimichan
 */
public class PatternPlugin implements PipelineLexerPlugin {

    private Pattern pattern;

    public static PatternPlugin of(Pattern pattern) {
        return new PatternPlugin(pattern);
    }

    public PatternPlugin(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public void init(PipelineLexerBuilder builder) {
        builder.addProcessor(new PatternWordpathProcessor(pattern));
    }

}
