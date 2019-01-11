package com.mayabot.nlp.segment.cws;

import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.PipelineTokenizerBuilder;
import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.core.ViterbiBestPathAlgorithm;
import com.mayabot.nlp.segment.plugins.AtomSplitAlgorithm;
import com.mayabot.nlp.segment.plugins.Correction;
import com.mayabot.nlp.segment.plugins.CustomDictionaryRecognition;
import com.mayabot.nlp.segment.plugins.PartOfSpeechTagging;
import com.mayabot.nlp.segment.plugins.correction.CorrectionWordpathProcessor;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.pos.PosPerceptronProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 基于感知机模型的分词器
 *
 * @author jimichan
 */
public class CWSTokenizerBuilder extends PipelineTokenizerBuilder
        implements CustomDictionaryRecognition, PartOfSpeechTagging, Correction {

    public static CWSTokenizerBuilder builder() {
        return new CWSTokenizerBuilder();
    }

    private boolean enablePOS = true;
    private boolean enableCorrection = true;
    private boolean enableCustomDictionary = false;

    public static void main(String[] args) {

        Mynlps.install(mynlpBuilder -> {
            mynlpBuilder.set(CwsService.cwsModelItem, CwsService.cswHanlpModel);
        });

        MynlpTokenizer tokenizer = CWSTokenizerBuilder.builder().build();

        Sentence sentence = tokenizer.parse("经过长时间的感情探索和临时分居后");
        System.out.println(sentence);
        System.out.println(tokenizer.parse("央视网消息：2019年1月10日，外交部发言人陆慷主持例行记者会。\n" +
                "\n" +
                "　　记者问：中国驻加拿大大使卢沙野昨天（9日）发表的一篇文章引起了外界的关注。卢大使文章称，中方拘捕2名加拿大公民是对加方拘押孟晚舟的报复，称中方举措系自卫行为。这是否相当于承认了中方拘捕两名加拿大人是对加方的报复？"));
    }

    /**
     * 在这里装配所需要的零件吧！！！
     */
    @Override
    protected void setUp() {

        //最优路径算法
        this.setBestPathComputer(ViterbiBestPathAlgorithm.class);

        //切词算法
        this.addWordSplitAlgorithm(CWSSplitAlgorithm.class);
        this.addWordSplitAlgorithm(AtomSplitAlgorithm.class);


        // Pipeline处理器

        if (enableCustomDictionary) {
            this.addProcessor(CustomDictionaryProcessor.class);
        }

        //分词纠错
        if (enableCorrection) {
            addProcessor(CorrectionWordpathProcessor.class);
        }

        //词性标注
        if (enablePOS) {
            addProcessor(PosPerceptronProcessor.class);
        }

    }

    public boolean isEnableCustomDictionary() {
        return enableCustomDictionary;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnableCustomDictionary(boolean enableCustomDictionary) {
        this.enableCustomDictionary = enableCustomDictionary;
        return this;
    }

    public boolean isEnablePOS() {
        return enablePOS;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnablePOS(boolean enablePOS) {
        this.enablePOS = enablePOS;
        return this;
    }

    public boolean isEnableCorrection() {
        return enableCorrection;
    }

    @NotNull
    @Override
    public CWSTokenizerBuilder setEnableCorrection(boolean enableCorrection) {
        this.enableCorrection = enableCorrection;
        return this;
    }
}
