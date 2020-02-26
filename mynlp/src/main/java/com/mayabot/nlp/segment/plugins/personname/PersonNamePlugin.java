package com.mayabot.nlp.segment.plugins.personname;

import com.mayabot.nlp.segment.pipeline.PipelineLexerBuilder;
import com.mayabot.nlp.segment.pipeline.PipelineLexerPlugin;

/**
 * 人名识别插件。
 * 3.1.0 新增了Processor。和PersonNameAlgorithm并存。
 * PersonNameAlgorithm处理长度小于等于3的人名。其他的人名，如果没有破坏其他词汇的切分，
 * 那么合并和为人名。
 * 修复了这种类型的bug
 * 阿里/nr 云/u 仓库/n 地址/n 正确/a ,/w 陈宝奇/nr 怪/a 别人/r 不好/a
 * 以前会把 阿里云仓 认为是人名。
 * 陈宝 奇怪 别人 ，人名又会被忽略的问题。
 * @author jimichan
 */
public class PersonNamePlugin implements PipelineLexerPlugin {

    public static final String key = "__person_name__";

    @Override
    public void install(PipelineLexerBuilder builder) {
        builder.addWordSplitAlgorithm(PersonNameAlgorithm.class);
        builder.addProcessor(PersonNameProcessor.class);
    }

}
