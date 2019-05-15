package com.mayabot.nlp.segment.plugins.collector;

/**
 * 收集器输出序列的模式。
 *
 * @author jimichan
 */
public enum TermCollectorModel {
    /**
     * 输出第一层的大词汇。"北京大学排名" => [北京大学] [排名]
     */
    TOP,

    /**
     * 输出第二层的原子词汇。"北京大学排名" => [北京] [大学] [排名]
     */
    ATOM,

    /**
     * 混合第一层和第二层。"北京大学排名" => [北京大学] [北京] [大学] [排名]
     */
    MIXED
}
