package com.mayabot.nlp.segment;


import java.io.Reader;
import java.util.stream.Stream;

/**
 * 这个类知道如何去处理WordTerm序列。
 * 初始化需要告诉他一个切词器。
 * 这个类也是无状态的，一个逻辑只需要一份。
 *
 */
public interface MynlpAnalyzer {

    Iterable<WordTerm> parse(Reader reader);

    Stream<WordTerm> stream(Reader reader);

}
