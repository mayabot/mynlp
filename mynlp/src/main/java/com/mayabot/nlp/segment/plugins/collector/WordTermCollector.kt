package com.mayabot.nlp.segment.plugins.collector

import com.mayabot.nlp.segment.WordTerm
import com.mayabot.nlp.segment.wordnet.Wordnet
import com.mayabot.nlp.segment.wordnet.Wordpath
import java.util.function.Consumer

/**
 * Mynlp WordTerm 收集器
 *
 *
 * 从wordPath、wordnet这两个数据结构中获得最终的分词结果。
 *
 *
 * 通过这个接口，可以让相同的分词器，获得不同的用途的分词结果。
 *
 * @author jimichan
 */
interface WordTermCollector {

    /**
     * 收集分词结果，最终发送到consumer中。
     * 这样外面是流水线还是list保存结果，由外部决定。
     *
     * @param txtChars  词图
     * @param KeepChar  词图
     * @param wordnet  词图
     * @param wordPath 最后的WordPath路径
     * @param consumer 接受WordTerm的消费者
     */
    fun collect(txtChars:CharArray?,wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>)


}

