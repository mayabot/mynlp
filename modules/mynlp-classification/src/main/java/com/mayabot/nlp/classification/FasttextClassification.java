package com.mayabot.nlp.classification;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mayabot.mynlp.fasttext.*;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.Lexers;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * 使用Fasttext算法的文本分类工具
 * <p>
 * 文本文件格式
 *
 * @author jimichan
 */
public class FasttextClassification {

    /**
     * 训练Fasttext模型
     */
    public static FastText train(File file, int dim, double learnRate, int epoch) throws Exception {
        TrainArgs trainArgs = new TrainArgs();
        trainArgs.setDim(dim);
        trainArgs.setLr(learnRate);
        trainArgs.setEpoch(epoch);

        return train(file, trainArgs);
    }

    /**
     * 训练Fasttext模型
     */
    public static FastText train(File file, TrainArgs trainArgs) throws Exception {

        Splitter splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults();


        LexerReader analyzer = Lexers.coreBuilder().build().filterReader(true, true);

        FileTrainExampleSource source = new FileTrainExampleSource(
                new WordSplitter() {
                    @NotNull
                    @Override
                    public List<String> split(String text) {
                        List<String> result = Lists.newArrayList();
                        splitter.split(text).forEach(part -> {
                            if (part.startsWith("__label__")) {
                                result.add(part);
                            } else {
                                for (String word : analyzer.scan(part).toWordSequence()) {
                                    result.add(word);
                                }
                            }
                        });
                        return result;
                    }
                }
                ,
                file
        );
        return FastText.train(source, ModelName.sup, trainArgs);
    }


    /**
     * 使用乘积量化压缩Fasttext模型，稍微损失一些精度.
     *
     * @param fastText
     * @return FastText
     * @throws Exception
     */
    public static FastText compress(FastText fastText) throws Exception {
        return FastText.quantize(fastText);
    }

    /**
     * 便捷方法，返回预测的第一个结果
     *
     * @param model
     * @param tokenizer 分词器
     * @param text      需要分析的原始文本
     * @return 分类目标. null表示没有结果
     */
    public static List<FloatStringPair> predict(FastText model, LexerReader tokenizer, String text, int top) {
        List<String> inputList = Lists.newArrayList(tokenizer.scan(text).toWordSequence());
        if (inputList.isEmpty()) {
            return null;
        }

        return model.predict(inputList, top);
    }

    /**
     * 预测一个唯一的目标，要求概率大于0.5f
     *
     * @param model
     * @param tokenizer
     * @param text
     * @return FloatStringPair
     */
    public static FloatStringPair predictOne(FastText model, LexerReader tokenizer, String text) {
        List<String> inputList = Lists.newArrayList(tokenizer.scan(text).toWordSequence());
        if (inputList.isEmpty()) {
            return null;
        }

        List<FloatStringPair> list = model.predict(inputList, 5);

        if (list.isEmpty()) {
            return null;
        }

        FloatStringPair first = list.get(0);

        if (first.first < 0.5f) {
            return null;
        } else {
            return first;
        }
    }

    private static LexerReader lexerReader;

    public static List<FloatStringPair> predict(FastText model, String text, int top) {

        if (lexerReader == null) {
            lexerReader = Lexers.coreBuilder().build().filterReader(true,true);
        }

        return predict(model, lexerReader, text,top);
    }

    public static FloatStringPair predictOne(FastText model, String text) {

        if (lexerReader == null) {
            lexerReader = Lexers.coreBuilder().build().filterReader(true, true);
        }

        return predictOne(model, lexerReader, text);
    }
}
