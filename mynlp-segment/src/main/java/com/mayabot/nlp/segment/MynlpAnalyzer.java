package com.mayabot.nlp.segment;


import java.io.Reader;
import java.util.stream.Stream;

/**
 *
 * MynlpAnalyzer主要面向Reader进行分词，里面包含了WordTerm序列的处理逻辑，可以进行停用词、标点符号的过滤。
 * 是无状态的，可多线程调用。
 * 具体参考StandardMynlpAnalyzer。用户自定义实现该接口需从BaseMynlpAnalyzer继承。
 *
 * @see com.mayabot.nlp.segment.analyzer.BaseMynlpAnalyzer
 * @see com.mayabot.nlp.segment.analyzer.StandardMynlpAnalyzer
 * @author jimichan jimichan@gmail.com
 */
public interface MynlpAnalyzer {

    /**
     * 对文本进行分词，返回一个延迟计算的Iterable&lt;Term&gt;。
     *
     * @param reader 文本源
     * @return 可迭代的WordTerm序列
     */
    Iterable<WordTerm> parse(Reader reader);

    /**
     * 对文本进行分词，返回一个延迟计算的Iterable&lt;Term&gt;。
     *
     * @param text
     * @return
     */
    Iterable<WordTerm> parse(String text);

    /**
     * 对文本进行分词，返回一个延迟计算的StreamWord&lt;Term&gt;。
     *
     * @param reader 文本源
     * @return 可迭代的WordTerm序列
     */
    Stream<WordTerm> stream(Reader reader);

    /**
     * 对文本进行分词，返回一个延迟计算的StreamWord&lt;Term&gt;。
     *
     * @param reader
     * @return
     */
    Stream<WordTerm> stream(String reader);

}
