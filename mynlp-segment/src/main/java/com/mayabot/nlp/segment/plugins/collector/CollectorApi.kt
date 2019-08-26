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
     * 收集分词结构
     *
     * @param wordnet  词图
     * @param wordPath 最后的WordPath路径
     * @param consumer 接受WordTerm的消费者
     */
    fun collect(wordnet: Wordnet, wordPath: Wordpath, consumer: Consumer<WordTerm>)

    var pickUpSubword: PickUpSubword?

    var fillSubword: FillSubword?


    /**
     * 感知机、crf等分词，wordnet中没有子词信息。那么通过这个接口在收集结果之前，通过词典新增子词信息。
     * @author jimichan
     */
    interface FillSubword {
        fun fill(wordnet: Wordnet, wordPath: Wordpath)
    }

    /**
     * 从wordnet中计算出子词的方法。
     * @author jimichan
     */
    interface PickUpSubword {

        fun pickup(term: WordTerm, wordnet: Wordnet, wordPath: Wordpath)
    }


}

