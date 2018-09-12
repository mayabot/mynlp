package com.mayabot.nlp.classification;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mayabot.mynlp.fasttext.*;
import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpAnalyzers;
import com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer;
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

    public static void main(String[] args) {
        List<String> strings = new StandardMynlpAnalyzer().parseToStringList("非常一般的酒店最过分的是早餐居然在地下负一层而且基本没什么吃的!酒店的位置也非常一般四周景色非常不好!");

        System.out.println(strings);

    }

    /**
     * 训练Fasttext模型
     */
    public static FastText train(File file, int dim, double learnRate, int epoch) throws Exception {
        TrainArgs trainArgs = new TrainArgs();
        trainArgs.setDim(100);
        trainArgs.setLr(0.05);
        trainArgs.setEpoch(10);

        return train(file, trainArgs);
    }

    /**
     * 训练Fasttext模型
     */
    public static FastText train(File file, TrainArgs trainArgs) throws Exception {

        Splitter splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults();


        MynlpAnalyzer analyzer = MynlpAnalyzers.standard();

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
                                List<String> list = analyzer.parseToStringList(part);

                                result.addAll(list);
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
     * @return
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
    public static String predict(FastText model, MynlpAnalyzer tokenizer, String text) {
        List<String> inputList = tokenizer.parseToStringList(text);
        if (inputList.isEmpty()) {
            return null;
        }
        List<FloatStringPair> result = model.predict(inputList, 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0).second;
        }
    }

    public static MynlpAnalyzer mynlpAnalyzer;

    public static String predict(FastText model, String text) {
        if (mynlpAnalyzer == null) {
            mynlpAnalyzer = MynlpAnalyzers.standard();
        }

        return predict(model, mynlpAnalyzer, text);
    }

}
