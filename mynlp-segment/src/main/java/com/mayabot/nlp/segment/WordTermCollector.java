package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.wordnet.Wordnet;
import com.mayabot.nlp.segment.wordnet.Wordpath;

import java.util.function.Consumer;

/**
 * Mynlp WordTerm 收集器
 *
 * 从wordPath、wordnet这两个数据结构中获得最终的分词结果。
 *
 * 通过这个接口，可以让相同的分词器，获得不同的用途的分词结果。
 *
 * @author jimichan
 */
public interface WordTermCollector {

    /**
     * 收集分词结构
     * @param wordnet  词图
     * @param wordPath 最后的WordPath路径
     * @param consumer 接受WordTerm的消费者
     */
    void collect(Wordnet wordnet, Wordpath wordPath, Consumer<WordTerm> consumer);

}
