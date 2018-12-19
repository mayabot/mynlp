package com.mayabot.nlp.segment.core;

import com.mayabot.nlp.segment.PipelineTokenizerBuilder;
import com.mayabot.nlp.segment.plugins.CommonRuleWordpathProcessor;
import com.mayabot.nlp.segment.plugins.CommonSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.TimeSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.correction.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.personname.PersonNameAlgorithm;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;

/**
 * 基于HMM-BiGram的分词器.
 * BiGram
 *
 * @author jimichan
 */
public class CoreTokenizerBuilder extends PipelineTokenizerBuilder {

    public static CoreTokenizerBuilder builder() {
        return new CoreTokenizerBuilder();
    }

    /**
     * 是否启用人名识别
     */
    private boolean personName = true;

    /**
     * 是否启用地名识别
     */
    private boolean placeName = true;

    /**
     * 是否启用组织结构名识别
     */
    private boolean orgName = true;


    private boolean pos = true;

    private boolean email = false;

    public boolean isPersonName() {
        return personName;
    }

    public CoreTokenizerBuilder setPersonName(boolean personName) {
        this.personName = personName;
        return this;
    }

    /**
     * 在这里装配所需要的零件吧！！！
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        this.addWordSplitAlgorithm(
                CoreDictionarySplitAlgorithm.class,
                CommonSplitAlgorithm.class,
                TimeSplitAlgorithm.class
        );

        if (personName) {
            addWordSplitAlgorithm(PersonNameAlgorithm.class);
        }

        // Pipeline处理器
        this.addProcessor(CustomDictionaryProcessor.class);

        this.addProcessor(CommonRuleWordpathProcessor.class);


        //分词纠错
        addProcessor(CorrectionWordpathProcessor.class);

        if (pos) {
            addProcessor(PosPerceptronProcessor.class);
        }

        //一些通用模式识别的处理
        addProcessor(CommonRuleWordpathProcessor.class);

        config(CommonRuleWordpathProcessor.class, x -> {
            x.setEnableEmail(email);
        });
    }


    /**
     * 词性分析开关
     *
     * @param pos
     * @return
     */
    public CoreTokenizerBuilder setPos(boolean pos) {
        this.pos = pos;
        return this;
    }

}
